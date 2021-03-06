package es.ucm.fdi.iw.parser;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.servlet.ModelAndView;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.transfer.UserTransfer;
import es.ucm.fdi.iw.util.DateUtil;

public class UserParser extends Parser {
	
	private static UserParser instance;
	
	public static UserParser getInstance() {
		if(instance == null)
			instance = new UserParser();
		return instance;
	}

    private static final String EMAIL_PATTERN = "^[^@]+@[^@]+\\.[a-zA-Z]{2,}$";
    private static final String EMAIL_EXAMPLE = "ejemplo@ejemplo.es";
    private static final String NAME_PATTERN = "^[\\w\\s]+$";
    private static final String PASSWORD_PATTERN = "^(?=.{"+UserParser.PASSWORD_MIN_LENGTH+",})(?=.*\\d)(?=.*[A-Z]).*$";
    private static final String NICKNAME_PATTERN = "^[A-Za-z]+$";
    private static final int EMAIL_MIN_LENGTH = 5;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int BIRTHDAY_MIN_AGE = 18;

    private static final int USER_ERROR_CODE = 3000;
    
    //Email
    private static final int USER_EMAIL_ERROR_CODE = 100;
    private static final int PARSE_COD_EMAIL_PATTERN = USER_ERROR_CODE+USER_EMAIL_ERROR_CODE+1;
    private static final String PE_MSG_EMAIL_PATTERN = "Invalid email, should be like: "+EMAIL_EXAMPLE;

    //Name
    private static final int USER_NAME_ERROR_CODE = 200;
    private static final int PARSE_COD_NAME_PATTERN = USER_ERROR_CODE+USER_NAME_ERROR_CODE+1;
    private static final String PE_MSG_NAME_PATTERN = "Invalid name, it can only contains letters";
    
    //Passwords
    private static final int USER_PASSWORD_ERROR_CODE = 300;
    
    private static final int PARSE_COD_PASSWORD_LENGTH = USER_ERROR_CODE+USER_PASSWORD_ERROR_CODE+1;
    private static final String PE_MSG_PASSWORD_LENGTH = "Password must contains at least "+PASSWORD_MIN_LENGTH+" characters";

    private static final int PARSE_COD_PASSWORD_PATTERN = USER_ERROR_CODE+USER_PASSWORD_ERROR_CODE+2;
    private static final String PE_MSG_PASSWORD_PATTERN = "Password must contains 1 capital letter, 1 number and at least "+PASSWORD_MIN_LENGTH+" characters";
    
    private static final int PARSE_COD_PASSWORDS_DINDT_MATCH = USER_ERROR_CODE+USER_PASSWORD_ERROR_CODE+3;
    private static final String PE_MSG_PASSWORDS_DINDT_MATCH = "Password mismatch";

    //Birthday
    private static final int USER_BIRTHDAY_ERROR_CODE = 400;
    private static final int PARSE_COD_BIRTHDAY_MENOR_EDAD = USER_ERROR_CODE+USER_BIRTHDAY_ERROR_CODE+1;
    private static final String PE_MSG_BIRTHDAY_MENOR_EDAD = "You must be "+BIRTHDAY_MIN_AGE+" years old";


    // ---- CHECKS ---- //

    public UserParser() {}

    //Email
    public static boolean isValidEmail(String email) throws ParseException {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches())//"x@x.x"
            throw new ParseException(PE_MSG_EMAIL_PATTERN, PARSE_COD_EMAIL_PATTERN);

        return true;
    }

    public static boolean isValidEmailLength(String email) throws ParseException {
        if(email.length() < EMAIL_MIN_LENGTH)
            throw new ParseException(PE_MSG_EMAIL_PATTERN, PARSE_COD_EMAIL_PATTERN);

        return true;
    }
    
    //Nickname
    public static boolean isValidNickname(String nickname) throws ParseException {
        Pattern pattern = Pattern.compile(NICKNAME_PATTERN);
        Matcher matcher = pattern.matcher(nickname);
        if (!matcher.matches())
            throw new ParseException(PE_MSG_NAME_PATTERN, PARSE_COD_NAME_PATTERN);

        return true;
    }

    //Nombre
    public static boolean isValidName(String nombre) throws ParseException {
        Pattern pattern = Pattern.compile(NAME_PATTERN);
        Matcher matcher = pattern.matcher(nombre);
        if (!matcher.matches())
            throw new ParseException(PE_MSG_NAME_PATTERN, PARSE_COD_NAME_PATTERN);

        return true;
    }

    //Passwords
    public static boolean isValidPassword(String password) throws ParseException {
        if (password.length() < PASSWORD_MIN_LENGTH)
            throw new ParseException(PE_MSG_PASSWORD_LENGTH, PARSE_COD_PASSWORD_LENGTH);
        
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches())
            throw new ParseException(PE_MSG_PASSWORD_PATTERN, PARSE_COD_PASSWORD_PATTERN);
        
        return true;
    }

    //Same pass
    public static boolean isValidSamePasswords(String password, String samePass) throws ParseException {
        if (!password.equalsIgnoreCase(samePass))
            throw new ParseException(PE_MSG_PASSWORDS_DINDT_MATCH, PARSE_COD_PASSWORDS_DINDT_MATCH);

        return true;
    }

    //Fecha Nacimiento
    public static boolean isValidBirthday(Date fechaNacimiento) throws ParseException {
        Calendar calendarMayorEdad = Calendar.getInstance();

        int anio = Calendar.getInstance().get(Calendar.YEAR) - BIRTHDAY_MIN_AGE;
        calendarMayorEdad.set(Calendar.YEAR, anio);

        Date fechaMayorEdad = calendarMayorEdad.getTime();

        int resCompareDates = fechaNacimiento.compareTo(fechaMayorEdad);

        if (resCompareDates > 0)
            throw new ParseException(PE_MSG_BIRTHDAY_MENOR_EDAD, PARSE_COD_BIRTHDAY_MENOR_EDAD);

        return true;//<=
    }

    // ---- PROCESAR ---- //

    public ParserResponse processEmail(String email) {
        ParserResponse response = null;
    	String msg = null;
        boolean emailOk = false;

        try {
        	emailOk = (
            		Parser.isNotNull(email)
                    && Parser.isNotEmptyString(email)
                    && UserParser.isValidEmailLength(email)
                    && UserParser.isValidEmail(email)
            );
        } catch(ParseException pe) {
           msg = pe.getMessage();
        }

        if(emailOk) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("email", email);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }

    public ParserResponse processNickname(String nickname) {
        ParserResponse response = null;
    	String msg = null;
    	boolean nameOk = false;

        try {
        	nameOk = (
            		Parser.isNotNull(nickname)
                    && Parser.isNotEmptyString(nickname)
                    && UserParser.isValidNickname(nickname)
            );
        } catch(ParseException pe) {
        	 msg = pe.getMessage();
        }
        
        if(nameOk) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("nickname", nickname);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }
    
    public ParserResponse processName(String name) {
        ParserResponse response = null;
    	String msg = null;
    	boolean nameOk = false;

        try {
        	nameOk = (
            		Parser.isNotNull(name)
                    && Parser.isNotEmptyString(name)
                    && UserParser.isValidName(name)
            );
        } catch(ParseException pe) {
        	 msg = pe.getMessage();
        }
        
        if(nameOk) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("name", name);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }

    public ParserResponse processPassword(String password) {
        ParserResponse response = null;
    	String msg = null;
        boolean passwordOK = false;

        try {
            passwordOK = (
            		Parser.isNotNull(password)
                    && Parser.isNotEmptyString(password)
                    && UserParser.isValidPassword(password)
            );
        } catch(ParseException pe) {
            msg = pe.getMessage();
        }

        if(passwordOK) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("password", password);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }

    public ParserResponse processPasswordAndSamePass(String password, String samePass) {
        ParserResponse response = null;
    	String msg = null;
    	boolean samePasswordOK = false;

        try {
            samePasswordOK = (
            		Parser.isNotNull(samePass)
                    && Parser.isNotEmptyString(samePass)
                    && UserParser.isValidPassword(samePass)
                    && UserParser.isValidSamePasswords(password, samePass)
            );
        } catch(ParseException pe) {
        	msg = pe.getMessage();
        }

        if(samePasswordOK) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("password", password);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }

    public ParserResponse processBirthday(String birthdayStr) {
        ParserResponse response = null;
    	String msg = null;
    	boolean birthdayOk = false;
    	Date birthday = null;


         try {
         	birthdayOk = (Parser.isNotNull(birthdayStr)
                     && Parser.isNotEmptyString(birthdayStr));

             if(birthdayOk) {
                 birthday = Parser.parseDate(birthdayStr);
                 birthdayOk = UserParser.isValidBirthday(birthday);
             }
        } catch(ParseException pe) {
        	msg = pe.getMessage();
        	birthdayOk = false;
        }
        
        if(birthdayOk) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("birthday", birthday);
        	response = new ParserResponse().parserOKResponse("", args);
        }
        else {
        	response = new ParserResponse().parserFailResponse(msg, null);
        }

        return response;
    }
    
    public boolean parseUserRegister(ModelAndView modelAndView, UserTransfer userTransfer) {
    	ParserResponse responseEmail = this.processEmail(userTransfer.getEmail());
		
		if(!responseEmail.isOk()) {
			modelAndView.addObject("emailError", responseEmail.getMessage());
		}
		
		ParserResponse responseNickname = this.processNickname(userTransfer.getNickname());
		
		if(!responseNickname.isOk()) {
			modelAndView.addObject("nickNameError", responseNickname.getMessage());
		}
		
		ParserResponse responsePassword = null;
		if(userTransfer.getPassword() != null && userTransfer.getPassword() != "") {
			responsePassword = this.processPassword(userTransfer.getPassword());
			
			if(!responsePassword.isOk()) {
				modelAndView.addObject("passwordError", responsePassword.getMessage());
			}
		}
		
		ParserResponse responseSamePassword = null;
		if(userTransfer.getSamePassword() != null && userTransfer.getSamePassword() != "") {
			responseSamePassword = this.processPasswordAndSamePass(userTransfer.getPassword(), userTransfer.getSamePassword());
			
			if(!responseSamePassword.isOk()) {
				modelAndView.addObject("samePasswordError", responseSamePassword.getMessage());
			}
		}
		
		return responseEmail.isOk() 
				&& responseNickname.isOk()
				&& (responsePassword != null ? responsePassword.isOk() : true) 
				&& (responseSamePassword != null ? responseSamePassword.isOk() : true);
    }
    
    public boolean parseUserModify(ModelAndView modelAndView, UserTransfer user) {
    	
		ParserResponse responseEmail = this.processEmail(user.getEmail());
		
		if(!responseEmail.isOk()) {
			modelAndView.addObject("emailError", responseEmail.getMessage());
		}

		ParserResponse responseName = this.processName(user.getName());
		
		if(!responseName.isOk()) {
			modelAndView.addObject("nameError", responseName.getMessage());
		}
		
		ParserResponse responseLastName = this.processName(user.getLastName());
		
		if(!responseLastName.isOk()) {
			modelAndView.addObject("lastNameError", responseLastName.getMessage());
		}
		
		String birthday = user.getBirthdateStr();
		if((birthday == null || birthday.equalsIgnoreCase("")) && user.getBirthdate() != null) {
			birthday = DateUtil.getDateStrWithoutHour(user.getBirthdate());
		}
		ParserResponse responseBirthday = this.processBirthday(birthday);
		
		if(!responseBirthday.isOk()) {
			modelAndView.addObject("birthdateError", responseBirthday.getMessage());
		}
		
		return responseEmail.isOk()
				&& responseName.isOk() 
				&& responseLastName.isOk()
				&& responseBirthday.isOk();
    }
    
    public boolean parseUserModifyPassword(ModelAndView modelAndView, UserTransfer userTransfer) {
    	
    	ParserResponse responseOldPassword = null;
		if(userTransfer.getPassword() != null && userTransfer.getPassword() != "") {
			responseOldPassword = this.processPassword(userTransfer.getPassword());
			
			if(!responseOldPassword.isOk()) {
				modelAndView.addObject("oldPasswordError", responseOldPassword.getMessage());
			}
		}
		
    	ParserResponse responseNewPassword = null;
		if(userTransfer.getPassword() != null && userTransfer.getPassword() != "") {
			responseNewPassword = this.processPassword(userTransfer.getPassword());
			
			if(!responseNewPassword.isOk()) {
				modelAndView.addObject("newPasswordError", responseNewPassword.getMessage());
			}
		}
		
		return responseOldPassword.isOk() && responseNewPassword.isOk();
    }
    
    public boolean userLoginDataCorrect(ModelAndView modelAndView, UserTransfer userLogin) {
    	
    	ParserResponse pResEmail = new ParserResponse();
    	
    	if(userLogin.getEmail() != null) {
    		pResEmail = processEmail(userLogin.getEmail());
    		
    		if(!pResEmail.isOk()) {
    			modelAndView.addObject("emailError", pResEmail.getMessage());
    		}
    	}
    	
    	ParserResponse pResPassw = new ParserResponse();
    	
    	if(userLogin.getPassword() != null) {
    		pResPassw = processPassword(userLogin.getPassword());
    		
    		if(!pResPassw.isOk()) {
    			modelAndView.addObject("passwordError", pResPassw.getMessage());
    		}
    	}
    	
    	return pResEmail.isOk() && pResPassw.isOk();
    }

}
