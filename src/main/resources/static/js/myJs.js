const togleSidebar = () => {
    if($(".sidebar").is(":visible")) {

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");

    }else {

        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
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
				window.location="/user/delete-contact/"+cid
			} else {
				swal("Your Conact  is safe!");
			}
		});
}
