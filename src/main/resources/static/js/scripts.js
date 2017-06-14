// Empty JS for your own code to be here



 // Highlight the actual page in the header-navigation (sends class="active" to nav)

        $(document).ready(function () {
            var action = window.location.pathname.split('/')[1];

            // Only slash in url (home)
            if (action == "") {
                $('ul.nav li:first').addClass('active');
            } else {
                // Highlight current menu
                $('ul.nav a[href="/' + action + '"]').parent().addClass('active');

                // Highlight parent menu item
                $('ul.nav a[href="/' + action + '"]').parents('li').addClass('active');
            }
        });