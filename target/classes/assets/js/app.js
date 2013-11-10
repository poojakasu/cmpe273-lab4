$(document).ready(function(){

	Validate();
});

$(":button").click(function() {		
	var isbn = this.id;
    $.ajax({
	    type:"PUT",
	    url: "library/v1/books/"+isbn+"?status=lost",
	    async:false,
	    complete: Load_View(isbn)
	    });	 
});

function Load_View(isbn){
	var tempStatus = "#"+isbn+"_status";
	var tempButton = "#"+isbn;
	$(tempStatus).html("lost");
	$(tempButton).attr("disabled", "disabled");
}

function Validate(){
	var txt = "Library:";
	var libInstance = $("#libHeader").html();
	if(libInstance.length> 0 && libInstance.split("-")[1] == "a")
		txt += "A";
	else
		txt += "B";
	$("#libHeader").html(txt);		
	var lststatus = $("p");
	for(var i=0;i<lststatus.length;i++){
		if($(lststatus[i]).html() == "lost"){
			var temp = lststatus[i].id;
			temp = temp.split('_')[0];
			var tempButton = "#"+temp;
			$(tempButton).attr("disabled", "disabled");
		}
			
	}
}

