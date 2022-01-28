console.log("this is script file")

const toggleSidebar=()=>{
	if($('.sidebar').is(":visible")) {
		
		/*for hide*/
		
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
		
	} else {
		/*for showing*/
		
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

const search=()=>{
	/*console.log("searching");*/
	
	let query=$("#search-result").val();
	
	if(query=="") {
		$(".search-result").hide();
		
	} else {
		console.log(query);
		// sending request to server
		
		let url = `http://localhost:9090/search/${query}`;
		
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			// data......
			//console.log(data);
			
			let text = `<div class='list-group'>`
			
				data.forEach((contact) => {
					text+=`<a href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
				});
			text+='</div>'
			
			$(".search-result").html(text);
			$(".search-result").show();
		});
	}
};