(function() {

    function main() {
    	var box = document.querySelectorAll("#box")[0];
    	box.addEventListener("click", clickHandler);
    }

	function clickHandler(e) {
		var box = e.target;
		if (box.classList.contains("active")) {
			box.classList.remove("active");
		} else {
			box.classList.add("active");
		}
	}

    document.addEventListener("DOMContentLoaded", main);
})();