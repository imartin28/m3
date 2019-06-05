"use strict"
$(function() {
	
	$("#btnTogglePlayer").click(function() {
		let icon = $(this).find("i");
	    if($(icon).text() === "keyboard_arrow_right") {//hide
	    	$(icon).text("keyboard_arrow_left");
	    	$(this).css("margin-left", "-20px");
	    }
	    else {//show
	    	$(icon).text("keyboard_arrow_right");
	    	$(this).css("margin-left", "-8px");
	    }
	    
	    let sidebar = $("#btnTogglePlayer").parent();
	    
	    let divPlayer = $(sidebar.find("div")[0]);
	    if($(divPlayer).hasClass("d-none"))
	    	$(divPlayer).removeClass("d-none");
	    else
	    	$(divPlayer).addClass("d-none");
	    
	    if(sidebar.hasClass("col-3")) {
	    	sidebar.removeClass("col-3");
	    	sidebar.addClass("col-0");
	    }
	    else {
	    	sidebar.removeClass("col-0");
	    	sidebar.addClass("col-3");
	    }
	});
	
	if($("#msg").text() && $("#msg").text() != "") {
		$("#msgModal").modal("toggle");
	}
	
	$("#msg").change(function() {
		$("#msgModal").modal("toggle");
	});	
	
});
	
