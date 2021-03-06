package es.ucm.fdi.iw.control;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.CFile;
import es.ucm.fdi.iw.model.FileType;
import es.ucm.fdi.iw.model.Friend;
import es.ucm.fdi.iw.model.Tag;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.UserFile;
import es.ucm.fdi.iw.service.FileService;
import es.ucm.fdi.iw.service.UserService;

import es.ucm.fdi.iw.util.JSONUtil;
import es.ucm.fdi.iw.util.MediaTypeUtils;
import es.ucm.fdi.iw.util.StringUtil;

@Controller()
@RequestMapping("file")
public class FileController {

	private static final Logger log = LogManager.getLogger(FileController.class);

	@Autowired
	private FileService fileService;

	@Autowired
	private UserService userService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private LocalData localData;

	/**
	 * Function to notify the current user a message from server the modal is
	 * located on nav.html in order to display a message in any view
	 * 
	 * @param model
	 * @param errorTitle
	 * @param errorMsg
	 */
	private void notifyModal(ModelAndView modelAndView, String title, String msg) {
		if (title != null && title != "" && msg != null && msg != "") {
			modelAndView.addObject("title", StringUtil.convertToTitleCaseSplitting(title));
			modelAndView.addObject("msg", msg);
		}
	}

	@GetMapping("/")
	public String index(ModelAndView modelAndView, HttpSession session) {
		return "index";
	}

	@GetMapping("/contents/{id}")
	public StreamingResponseBody getFile(@PathVariable long id, Model model, HttpSession session)
			throws IOException {
		CFile file = fileService.findById(id);
		Long userId = ((User) session.getAttribute("u")).getId();
		// ojo con acceso: no basta con saber el id del fichero
		File f = localData.getFile("files", file.getSha256() + "." + file.getExtension());
		InputStream in;

		in = new BufferedInputStream(new FileInputStream(f));

		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream os) throws IOException {
				FileCopyUtils.copy(in, os);
			}
		};
	}
	
	@GetMapping("/file/{id}")
	@ResponseBody
	public String getFile(ModelAndView modelAndView, HttpSession session, @PathVariable("id") Long fileId)
			throws IOException {

		String response = "File not found";

		CFile file = fileService.findById(fileId);
		User currentUser = (User) session.getAttribute("u");

		if (file != null && currentUser != null) {
			JSONObject fileJSONObject = cfileToJsonObject(file);
			if(fileJSONObject != null)
				response = fileJSONObject.toString();
		}

		return response;
	}
	
	private JSONObject cfileToJsonObject(CFile file) {
		if(file == null)
			return null;
			
		JSONObject fileJSONObject = new JSONObject();
		try {
			fileJSONObject.put("id", file.getId());
			fileJSONObject.put("name", file.getName());
			fileJSONObject.put("url", "/file/contents/"+file.getId());
			fileJSONObject.put("mimetype", file.getMimetype());
			
			List<JSONObject> tagsJson = new ArrayList<JSONObject>();
			if(file.getTags() != null && !file.getTags().isEmpty()) {
				for(Tag t : file.getTags()) {
					if(t != null)
						tagsJson.add(t.toDisplayJsonObject());
				}
			}
			fileJSONObject.put("tags", tagsJson);
			
			fileJSONObject.put("metadata", file.getMetadata());
			
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
		
		return fileJSONObject;
	}
	
	@GetMapping("/tag/{id}")
	@ResponseBody
	public String getTag(ModelAndView modelAndView, HttpSession session, @PathVariable("id") Long fileId)
			throws IOException {

		String response = "File not found";

		User currentUser = (User) session.getAttribute("u");
		Long userId = ((User) session.getAttribute("u")).getId();
		
		if(currentUser != null && userId != null) {
			List<Tag> tags = entityManager.createNamedQuery("findByIdAndUserId", Tag.class)
					.setParameter("id", fileId)
					.setParameter("userId", userId)
					.getResultList();
			Tag tag = (tags != null && !tags.isEmpty() ? tags.get(0) : null);
	
			if (tag != null && currentUser != null) {
				JSONObject tagJSONObject = tagToJsonObject(tag);
				if(tagJSONObject != null)
					response = tagJSONObject.toString();
			}
		}

		return response;
	}
	
	private JSONObject tagToJsonObject(Tag tag) {
		if(tag == null)
			return null;
			
		JSONObject tagJSONObject = new JSONObject();
		try {
			tagJSONObject.put("id", tag.getId());
			tagJSONObject.put("name", tag.getName());
			tagJSONObject.put("color", tag.getColor());

			List<JSONObject> files = new ArrayList<JSONObject>();
			if(tag.getFiles() != null && !tag.getFiles().isEmpty()) {
				for(CFile f : tag.getFiles()) {
					JSONObject fileJson = cfileToJsonObject(f);
					if(fileJson != null)
						files.add(fileJson);
				}
			}
			tagJSONObject.put("files",files);
			
		} catch (JSONException e) {
			log.error(e.getMessage());
		}
		
		return tagJSONObject;
	}

	@GetMapping("/show/{id}")
	public ModelAndView getFileView(ModelAndView modelAndView, HttpSession session,
			@PathVariable("id") Long fileId) throws IOException {

		String err = "File not found";

		CFile file = fileService.findById(fileId);
		User currentUser = (User) session.getAttribute("u");

		if (file != null && currentUser != null) {
			err = null;
			modelAndView.addObject("filename", file.getName());

			String mimetype = (file.getMimetype() != null && !file.getMimetype().equalsIgnoreCase("")
					? file.getMimetype().split("/")[0]
					: null);

			File f = localData.getFile("files", file.getSha256() + "." + file.getExtension());
			String url = f.getAbsolutePath() + (mimetype.equalsIgnoreCase(FileType.Video.getKeyName()) ? "#t=0.5" : "");

			modelAndView.addObject("fileId", fileId);

			if (mimetype != null && !mimetype.equalsIgnoreCase("")) {
				modelAndView.addObject("mimetype", mimetype);
			} else {
				err = "File has invalid MIME type";
			}

			modelAndView.addObject("tags", file.getTags());
			
			modelAndView.addObject("metadata", file.getMetadata());
			
			List<UserFile> permission = entityManager.createNamedQuery("findByIds", UserFile.class)
					.setParameter("id_user", currentUser.getId())
					.setParameter("id_file", fileId).getResultList();
			if (!permission.isEmpty())
				modelAndView.addObject("permission", permission.get(0).getPermission());
			else modelAndView.addObject("permission", "r");
			
			List<Friend> friendships = entityManager.createNamedQuery("readFriendshipsOfUser", Friend.class)
					.setParameter("userId", currentUser.getId()).getResultList();
			modelAndView.addObject("friends", friendships);
		}

		String viewName = "redirect:/user/";
		if (err == null)
			viewName = "file";

		modelAndView.setViewName(viewName);

		return modelAndView;
	}
	
	@PostMapping("/share/{id}")
	@Transactional
	public ModelAndView shareFile(ModelAndView modelAndView, HttpSession session,
			@PathVariable("id") Long fileId, @RequestParam(name="receiver", required=false) String friendId) {
		String err = null;
		
		User user = (User) session.getAttribute("u");
		CFile file = fileService.findById(fileId);
		User friend = null;
		
		if (friendId != null)
			friend = userService.findById(Long.parseLong(friendId));
		
		if (friend != null && friend.isActive()) {
			List<UserFile> file_friend = entityManager.createNamedQuery("findByIds", UserFile.class)
					.setParameter("id_user", Long.parseLong(friendId))
					.setParameter("id_file", fileId).getResultList();
			
			if (file_friend.isEmpty()) {
				UserFile userFile = new UserFile(friend, file, "r");
				entityManager.persist(userFile);
			}
			else
				err = "Your friend already has access to that file";
		}
		else
			err = "Friend not found";
		
		modelAndView.setViewName("file");
		modelAndView.addObject("fileId", fileId);
		modelAndView.addObject("filename", file.getName());
		modelAndView.addObject("mimetype", file.getMimetype().split("/")[0]);
		modelAndView.addObject("tags", file.getTags());
		modelAndView.addObject("metadata", file.getMetadata());
		
		List<UserFile> permission = entityManager.createNamedQuery("findByIds", UserFile.class)
				.setParameter("id_user", user.getId())
				.setParameter("id_file", fileId).getResultList();
		modelAndView.addObject("permission", permission.get(0).getPermission());
		
		List<Friend> friendships = entityManager.createNamedQuery("readFriendshipsOfUser", Friend.class)
				.setParameter("userId", user.getId()).getResultList();
		modelAndView.addObject("friends", friendships);
		
		if (err != null)
			this.notifyModal(modelAndView, "Error", err);
		else
			this.notifyModal(modelAndView, "File shared", "You have successfully share your file");
		
		return modelAndView;
	}

	@PostMapping("/upload")
	@Transactional
	public String postFile(@RequestParam String sha256, @RequestParam MultipartFile file, Model model,
			HttpSession session, HttpServletRequest request, @RequestParam Long currentTagId) {
		
		User user = (User) session.getAttribute("u");
		model.addAttribute("user", user);

		User requester = (User) session.getAttribute("u");
		if (requester.getId() != user.getId()) {
			return "user";
		}

		log.info("Uploading file for user {}", user.getId());

		/*
		 * Comprobar que existen el directorio del usuario y el fichero, y crearlos en
		 * caso contrario
		 */

		File folder = localData.getFolder("files");
		List<CFile> filesBBDD = fileService.findAllBysha256(sha256);

		CFile fileToPersist = new CFile(sha256, file.getOriginalFilename(), file.getSize(), file.getContentType());
		
		
		
		if (currentTagId != null) {
			Tag currentTag = (Tag) entityManager.createNamedQuery("findById", Tag.class).setParameter("id", currentTagId)
					.getSingleResult();
			
			fileToPersist.getTags().add(currentTag);
			currentTag.getFiles().add(fileToPersist);
		}
		
		fileToPersist.setPath(
				folder.getAbsolutePath() + "/" + fileToPersist.getSha256() + "." + fileToPersist.getExtension());
		entityManager.persist(fileToPersist);
		
		UserFile userFile = new UserFile(user, fileToPersist, "rw");
		entityManager.persist(userFile);
		
		if (filesBBDD.size() == 0) {
			uploadFile(sha256, file, fileToPersist, user);
		}

		return "redirect:/user/";
	}

	private void uploadFile(String sha256, MultipartFile file, CFile fileToPersist, User user) {
		try {
			
			File f = new File(fileToPersist.getPath());

			try {
				if (f.createNewFile()) {
					System.out.println("File is created!");
				} else {
					System.out.println("File already exists.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (file.isEmpty()) {
				log.info("failed to upload file : empty file?");
			} else {
				try {
					// Guardar fichero en el disco
					InputStream fileStream = file.getInputStream();
					FileUtils.copyInputStreamToFile(fileStream, f);

					// Guardar datos del fichero en la base de datos

					log.info("Succesfully uploaded file for user {} into {}", user.getId(), f.getAbsolutePath());
				} catch (Exception e) {
					System.out.println("Error uploading file of user " + user.getId() + " " + e);
				}
			}

		} catch (Exception e) {
			log.warn("ERROR DESCONOCIDO", e);
		}

	}

	@RequestMapping(value="/download/{id}", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) throws IOException {

		CFile file = fileService.findById(id);
		MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, file.getName());

		File f = new File(file.getPath());
		InputStreamResource resource = new InputStreamResource(new FileInputStream(f));

		return ResponseEntity.ok()
				// Content-Disposition
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
				// Content-Type
				.contentType(mediaType)
				// Contet-Length
				.contentLength(f.length()) //
				.body(resource);
	}
	
	@RequestMapping(value="/zip", produces="application/zip")
	public void downloadZip(@RequestParam("idFiles") String json, HttpServletResponse response)
			throws IOException {
	    List<Long> ids = new ArrayList<Long>();
	    try {
			JSONArray idsJSON = new JSONArray(json);
			for (int i = 0; i < idsJSON.length(); i++)
				ids.add(idsJSON.getLong(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    
	    if (!ids.isEmpty()) {
	    	response.setStatus(HttpServletResponse.SC_OK);
		    response.addHeader("Content-Disposition", "attachment; filename=\"m3_files.zip\"");
		    
		    ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
		    
		    List<CFile> files = entityManager.createNamedQuery("findAllById", CFile.class)
					.setParameter("ids", ids).getResultList();

		    for (int i = 0; i < files.size(); i++) {
		    	File f = new File(files.get(i).getPath());
		    	
		        zipOutputStream.putNextEntry(new ZipEntry(files.get(i).getName()));
		        FileInputStream fileInputStream = new FileInputStream(f);

		        IOUtils.copy(fileInputStream, zipOutputStream);

		        fileInputStream.close();
		        zipOutputStream.closeEntry();
		    }
		    
		    zipOutputStream.close();
	    }
	    else
	    	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	@PostMapping("/modifyFileName")
	@Transactional
	public String postModifyFileName(@RequestParam("idFile") Long id, @RequestParam("fileName") String name) {

		CFile file = fileService.findById(id);
		file.setName(name);
		return "redirect:/user/";
	}
	
	@PostMapping("/modifyFileMetadata")
	@Transactional
	public String postModifyFileMetadata(ModelAndView modelAndView, HttpSession session,
			@RequestParam("fileId") Long id, @ModelAttribute ("metadata") String metadata)  {

		CFile file = fileService.findById(id);
		User currentUser = (User) session.getAttribute("u");

		if (file != null && currentUser != null) {
			if (metadata != "")
				file.setMetadata(metadata);
			else file.setMetadata("{}");
			fileService.save(file);
		}
		return "redirect:/file/show/" + id;
	}

	@PostMapping("/deleteFile")
	@Transactional
	public String postDeleteFile(@RequestParam("idFile") Long fileId, HttpSession session) {
		User currentUser = (User) session.getAttribute("u");
		CFile file = fileService.findById(fileId);
		
		List<UserFile> permission = entityManager.createNamedQuery("findByIds", UserFile.class)
				.setParameter("id_user", currentUser.getId())
				.setParameter("id_file", fileId).getResultList();
		
		if (permission.get(0).getPermission().contains("w")) {
			File f = new File(file.getPath());
			
			if (f.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}
			
			fileService.deleteFile(file);
		}
		else
			entityManager.remove(permission.get(0)); 
	
		return "redirect:/user/";
	}

	@PostMapping("/deleteFiles")
	@Transactional
	@ResponseBody
	public String deleteFiles(@RequestBody List<Long> ids, HttpSession session, HttpServletResponse response) {
		User currentUser = (User) session.getAttribute("u");
		List<CFile> files = entityManager.createNamedQuery("findAllById", CFile.class)
				.setParameter("ids", ids).getResultList();

		File f = null;

		for (int i = 0; i < files.size(); i++) {
			List<UserFile> permission = entityManager.createNamedQuery("findByIds", UserFile.class)
					.setParameter("id_user", currentUser.getId())
					.setParameter("id_file", files.get(i).getId()).getResultList();
			
			if (permission.get(0).getPermission().contains("w")) {
				f = new File(files.get(i).getPath());
				
				if (f.delete()) {
					System.out.println(files.get(i).getName() + " is deleted!");
				} else {
					System.out.println("Delete operation is failed.");
				}
				
				fileService.deleteFile(files.get(i));
			}
			else
				entityManager.remove(permission.get(0));
		}

		response.setStatus(200);
		return "{}";
	}

	@GetMapping("/share/{id}")
	public ModelAndView getShare(ModelAndView modelAndView, HttpSession session, @PathVariable("id") Long fileId)
			throws IOException {

		String err = "File not found";

		CFile file = fileService.findById(fileId);
		User currentUser = (User) session.getAttribute("u");

		if (file != null && currentUser != null) {
			err = null;
			modelAndView.addObject("filename", file.getName());

			String mimetype = (file.getMimetype() != null && !file.getMimetype().equalsIgnoreCase("")
					? file.getMimetype().split("/")[0]
					: null);

			String url = "/file/user" + currentUser.getId() + "/" + fileId + "." + file.getExtension()
					+ (mimetype.equalsIgnoreCase(FileType.Video.getKeyName()) ? "#t=0.5" : "");
			modelAndView.addObject("fileurl", url);

			if (mimetype != null && !mimetype.equalsIgnoreCase("")) {
				modelAndView.addObject("mimetype", mimetype);
			} else {
				err = "File has invalid MIME type";
			}

			modelAndView.addObject("tags", file.tagNameList());

			try {
				JSONObject metadataJSON = new JSONObject(file.getMetadata());
				Map<String, Object> metadataMapObject = JSONUtil.toMap(metadataJSON);
				modelAndView.addObject("metadata", metadataMapObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		String viewName = "redirect:/user/";
		if (err == null)
			viewName = "share";

		modelAndView.setViewName(viewName);

		return modelAndView;
	}

	@GetMapping("/edit/{id}")
	public ModelAndView getEdit(ModelAndView modelAndView, HttpSession session, @PathVariable("id") Long fileId)
			throws IOException {
		String err = "File not found";

		CFile file = fileService.findById(fileId);
		User currentUser = (User) session.getAttribute("u");

		if (file != null && currentUser != null) {
			err = null;
			modelAndView.addObject("filename", file.getName());

			String mimetype = (file.getMimetype() != null && !file.getMimetype().equalsIgnoreCase("")
					? file.getMimetype().split("/")[0]
					: null);

			File f = localData.getFile("files", file.getSha256() + "." + file.getExtension());
			String url = f.getAbsolutePath() + (mimetype.equalsIgnoreCase(FileType.Video.getKeyName()) ? "#t=0.5" : "");

			modelAndView.addObject("fileId", fileId);

			if (mimetype != null && !mimetype.equalsIgnoreCase("")) {
				modelAndView.addObject("mimetype", mimetype);
			} else {
				err = "File has invalid MIME type";
			}

			modelAndView.addObject("tags", file.getTags());

			modelAndView.addObject("metadata", file.getMetadata());
		}

		String viewName = "redirect:/user/";
		if (err == null)
			viewName = "modifyFile";

		modelAndView.setViewName(viewName);

		return modelAndView;
	}

}
