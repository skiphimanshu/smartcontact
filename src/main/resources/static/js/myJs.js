const togleSidebar = () => {
	if ($(".sidebar").is(":visible")) {

		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");

	} else {

		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};
function deleteContact(cid) {
	swal({
		title: "Are you sure?",
		text: "You want to delete this contact",
		icon: "warning",
		buttons: true,
		dangerMode: true,
	})
		.then((willDelete) => {
			if (willDelete) {
				window.location = "/user/delete-contact/" + cid
			} else {
				swal("Your Conact  is safe!");
			}
		});
}

function readURL(input) {
	if (input.files && input.files[0]) {
		var reader = new FileReader();

		reader.onload = function(e) {
			$('#blah')
				.attr('src', e.target.result)
				.width(30)
				.height(40);
		};

		reader.readAsDataURL(input.files[0]);
	}
}
const search =()=> {
	let query =$("#search-input").val();
	console.log("searching...");
	if(query=="") {
		
		$(".search-result").hide();

	}else {
		console.log(query);
		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then(response=>{


			return response.json();
		}).then(data=>{
			console.log(data);
			let text = `<div class='list-group'>`;
			data.forEach(contact=> {
				text += `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-action'> ${contact.nickname} </a>`
			});
			text+= `</div>`;
			$(".search-result").html(text);
			$(".search-result").show();

		});
		$(".search-result").show();
	}

};      