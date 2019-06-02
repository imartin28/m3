$(() => {
	$("#select-all-groups").on("change", selectAllGroupsCheckBoxHandler);
	$("#delete-groups").on("click", deleteGroupsButtonHandler);
	$('.btn-edit-group').on('click', editGroupModalHandler);
	$("#btn-add-members").on("click", addMembersButtonHandler);
	$("#chat-message-submit-button").on("click", chatMessageSubmitButtonHandler);
	$(".delete-user-from-group-button").on("click", deleteUserFromGroupButtonHandler);
	$(".delete-user-in-session-from-group-button").on("click", deleteUserInSessionFromGroupButtonHandler);
	$(".change-permission-button").on("click", changePermissionButtonHandler);
	$("#modal-btn-change-permission").on("click", changePermissionModalButtonHandler);
    $(".btn-view-members").on("click", showMembersHandler);
});
 


function showMembersHandler(){
	
	
	let groupId = $(this).attr('data-group-id');
	console.log(groupId);
	
	$.ajax({
		type:"GET",
		url: "/groups/viewMembers/" + groupId,
		dataType: 'json',
		headers: {
			"Content-Type": "application/json",				
			"X-CSRF-TOKEN": m3.csrf.value
		},
		success : function(data){
			showMembersInModal(data);
			
			console.log("exito");
		},
		error: function (jqXHR, textStatus, errorThrown) {
			
            console.log("Se ha producido un error: " + errorThrown);
       }
	});
	
}


function showMembersInModal(listOfMembers){
	
	let elemento;
	
	$("#members-list").empty();
	listOfMembers.forEach(member =>{
		
		elemento = "<li class=' d-flex flex-row'>" +
		"<label class='friendPicker'>" +
		"<span class='friendPicker'>" +
		"<img class='profile-image' src='" + member.avatar + "'>"+
		"<span class='d-flex flex-column align-items-center mr-3'>" +
		"<span class='link profile-name name'>" + member.name + "</span>" +
		"<span class='nick text-center'>" + member.nickname  + "</span>" +
		"</span></span></label></li>";
		$("#members-list").append(elemento);			 
		
	});
	
		
	
}


function chatMessageSubmitButtonHandler(event) {
	event.preventDefault();
}

function addMembersButtonHandler(){
	
	let membersChecked = $("input[name='members-check']:checked");
	
	let array_IdsAddMembers = [];
	
	membersChecked.each(function(){
		let member = $(this);		
		array_IdsAddMembers.push(member.val());		
	});
	
	
	let idGroup = $("#title-name-group").attr("data-group-id");
	array_IdsAddMembers.push(idGroup);
	
	peticionAjaxConListaIds(array_IdsAddMembers, "/groups/addMembers");
}


function selectAllGroupsCheckBoxHandler() {
	
	let inputs = $("input[name='group-check']");
	
	if(inputs !== null && inputs.length > 0) {
		for(let i = 0; i < inputs.length; i++) {
			if(!$(inputs[i]).prop("disabled")) {
				if( $(this).is(':checked') ){
					$(inputs[i]).prop("checked", true);
			    } else {
			    	$(inputs[i]).prop("checked", false);
			    }
			}
		}
	}
}


function editGroupModalHandler() {

	let groupId = $(this).attr('data-group-id');
	let groupName = $(this).attr('data-group-name');
	
	
	$("#modalEditGroup").find("#edit-group-id").val(groupId);
	$("#modalEditGroup").find("#edit-group-name").val(groupName);
	
}

function deleteGroupsButtonHandler() {
	let groupsChecked = $("input[name='group-check']:checked");
	
	let array_IdsToDelete = [];
	
	groupsChecked.each(function(){
		let group = $(this);		
		array_IdsToDelete.push(group.val());		
	});

	peticionAjaxConListaIds(array_IdsToDelete, "/groups/deleteGroups");
}



function peticionAjaxConListaIds(arrayIds, url) {
	$.ajax({
		type:"POST",
		url: url,
		data: JSON.stringify(arrayIds),
		dataType: 'json',
		headers: {
			"Content-Type": "application/json",				
			"X-CSRF-TOKEN": m3.csrf.value
		},
		success : function(){
			location.reload();
			console.log("exito");
		},
		error: function (jqXHR, textStatus, errorThrown) {
			 location.reload();
            console.log("Se ha producido un error: " + errorThrown);
       }
	});
}

function deleteUserInSessionFromGroupButtonHandler() {
	let userId = $(this).attr("data-user-id");
	let groupId = $(this).attr("data-group-id");
	deleteMember(true, userId, groupId);
}

function deleteUserFromGroupButtonHandler() {
	let userId = $(this).attr("data-user-id");
	let groupId = $(this).attr("data-group-id");
	deleteMember(false, userId, groupId);
}

function deleteMember(isUserInSession, userId, groupId) {
	
	
	$.ajax({
		type:"POST",
		url: "/groups/deleteMember",
		data: JSON.stringify({userId : userId, groupId : groupId}),
		dataType: 'json',
		headers: {
			"Content-Type": "application/json",				
			"X-CSRF-TOKEN": m3.csrf.value
		},
		success : function(){
			if (isUserInSession) {
				window.location = "http://localhost:8080/groups/";
			} else {
				location.reload();
			}
			
			console.log("exito");
		},
		error: function (jqXHR, textStatus, errorThrown) {
			if (isUserInSession) {
				window.location = "http://localhost:8080/groups/";
			} else {
				location.reload();
			}
            console.log("Se ha producido un error: " + errorThrown);
       }
	});
}


function changePermissionButtonHandler() {
	let userId = $(this).attr("data-user-id");
	
	$("#modalChangePermission").find("#change-permission-user-id").val(userId);
}

function changePermissionModalButtonHandler() {
	let userId = $("#modalChangePermission").find("#change-permission-user-id").val();
	let permission = $("#permission-select").find(":selected").text();
	let groupId = $("#title-name-group").attr("data-group-id");
	
	$.ajax({
		type:"POST",
		url: "/groups/changePermission",
		data: JSON.stringify({userId : userId, permission : permission, groupId : groupId}),
		dataType: 'json',
		headers: {
			"Content-Type": "application/json",				
			"X-CSRF-TOKEN": m3.csrf.value
		},
		success : function(){
			location.reload();
			console.log("exito");
		},
		error: function (jqXHR, textStatus, errorThrown) {
			location.reload();
            console.log("Se ha producido un error: " + errorThrown);
       }
	});
}
