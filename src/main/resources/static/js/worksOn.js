/**
 * Created by timo on 06.06.16.
 */

var foreignEmployees = [];

//Pickaday for Startdatum Enddatum
$(document).ready(function(){

    $(".pickaday").each(function() {
        new Pikaday({
            field: $(this).get(0),
            format: 'YYYY-MM-DD'});
    })
})


//
// Updates "Select all" control in a data table
//
function updateDataTableSelectAllCtrl(table){
    var $table             = table.table().node();
    var $chkbox_all        = $('tbody input[type="checkbox"]', $table);
    var $chkbox_checked    = $('tbody input[type="checkbox"]:checked', $table);
    var chkbox_select_all  = $('thead input[name="select_all"]', $table).get(0);

    // If none of the checkboxes are checked
    if($chkbox_checked.length === 0){
        chkbox_select_all.checked = false;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }

        // If all of the checkboxes are checked
    } else if ($chkbox_checked.length === $chkbox_all.length){
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }

        // If some of the checkboxes are checked
    } else {
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = true;
        }
    }
}


$(document).ready(function () {

    //call this first so we start out with the correct visibility depending on the selected form values
    $(".selectS").each(function() {
        toggleFields(this);
    })

    //this will call our toggleFields function every time the selection value of our underAge field changes
    $(".selectS").change(function () {
        toggleFields(this);
    });

});
//this toggles the visibility of our parent permission fields depending on the current selected value of the underAge field
function toggleFields(elem) {
    var jElem = $(elem);
    var index = jElem.attr("data-index");
    if (jElem.val() == "Ausfallzeit") {
        //hide project selection and set default value
        $("#pid").hide();
        $("#selectP").attr("value", "7000");
    } else if(jElem.val() == "Nicht angegeben") {
        $("#pid").hide();
        $("#selectP").attr("value", "7000");
    } else {
        $("#pid").show();
    }
}

//this checks if the employee was changed for the submit buttons (not ready yet)
$("selectE").on("change paste input click", function() {
     location.reload();
});


//this adds the field with the lastUrl
$(document).ready(function() {
    var referrer = document.referrer;
    if(!referrer.includes("worksOn")) {
        document.getElementById("lastUrl").value = referrer;
    }
});

function cancel() {
    var referrer = document.referrer;
    document.getElementById("cancel").href = referrer;
}

function sendMail() {

}

function prepareTable(table, rows_selected) {

    function rowClicked(element) {
        var $row = $(element).closest('tr');

        // Get row employee ID
        var rowId = $(table.row($row).node()).children().first().children().first().val();

        // Is row ID in the list of selected row IDs?
        var index = $.inArray(rowId, rows_selected);

        var myOrgaUnitId = $('#myOrgaUnitId').attr("data-orga_id");
        var emplOrgaUnitId = $(element).attr("data-orga_id");
        var emplName = $(element).attr("data-empl_name");

        // If checkbox is checked and row ID is not in list of selected row IDs
        if(element.checked && index === -1){
            rows_selected.push(rowId);

            if(myOrgaUnitId != emplOrgaUnitId) {
                foreignEmployees.push(emplName);
            }

            // Otherwise, if checkbox is not checked and row ID is in list of selected row IDs
        } else if (!element.checked && index !== -1){
            rows_selected.splice(index, 1);

            if(myOrgaUnitId != emplOrgaUnitId) {
                var emplIndex = $.inArray(emplName, foreignEmployees);
                if(emplIndex !== -1) {
                    foreignEmployees.splice(emplIndex, 1);
                }
            }
        }
        if(foreignEmployees.length > 0) {
            $('#requestDiv').show();
            $('#preRequestDiv').hide();
            $('#preSaveDiv').hide();
            $('#saveDiv').hide();
        } else {
            $('#requestDiv').hide();
            $('#preRequestDiv').hide();
            $('#preSaveDiv').hide();
            $('#saveDiv').show();
        }

        if(element.checked){
            $row.addClass('selected');
        } else {
            $row.removeClass('selected');
        }

        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(table);

        // Prevent click event from propagating to parent

    }
    
    // Handle click on checkbox
    $('#list tbody').on('click', 'input[type="checkbox"]', function(e){
        rowClicked(this);
        e.stopPropagation();
    });

    // Handle click on table cells with checkboxes
    $('#list').on('click', 'tbody td, thead th:first-child', function(e){
        $(this).parent().find('input[type="checkbox"]').trigger('click');
    });

    // Handle click on "Select all" control
    $('thead input[name="select_all"]', table.table().container()).on('click', function(e){
        if(this.checked){
            $('#example tbody input[type="checkbox"]:not(:checked)').trigger('click');
        } else {
            $('#example tbody input[type="checkbox"]:checked').trigger('click');
        }

        // Prevent click event from propagating to parent
        e.stopPropagation();
    });

    // Handle table draw event
    table.on('draw', function(){
        // Update state of "Select all" control
        updateDataTableSelectAllCtrl(table);
    });

    // Handle form submission event
    $('#worksOnForm').on('submit', function(e){
        var form = this;

        // Iterate over all selected checkboxes
        $.each(rows_selected, function(index, rowId){
            // Create a hidden element
            $(form).append(
                $('<input>')
                    .attr('type', 'hidden')
                    .attr('name', 'employeeIds')
                    .val(rowId)
            );
        });

        //// Output form data to a console
        //$('#example-console').text($(form).serialize());
        //console.log("Form submission", $(form).serialize());
        //
        //// Remove added elements
        //$('input[name="id\[\]"]', form).remove();
        //
        //// Prevent actual form submission
        //e.preventDefault();
    });
    
    
}

function putEmployeeNamesToModal() {
    var emplNames = "";
    $('#employeeNames').html(foreignEmployees.join(", "));
}

var oldSelectEmplName = "";
function addForeignEmployeeSelect() {
    var selectValue = $(this).val();
    var myOrgaUnitId = $('#myOrgaUnitId').attr("data-orga_id");
    var emplOrgaUnitId = $("#selectE option[value='" + selectValue + "']").attr("data-orga_id");
    var emplName = $("#selectE option[value='" + selectValue + "']").attr("data-empl_name");


    if(myOrgaUnitId != emplOrgaUnitId) {
        // Add new selected employee
        foreignEmployees.push(emplName);
    }

    // Remove former selected employee
    var emplIndex = $.inArray(oldSelectEmplName, foreignEmployees);
    if(emplIndex !== -1) {
        foreignEmployees.splice(emplIndex, 1);
    }

    oldSelectEmplName = emplName;

    if(foreignEmployees.length > 0) {
        $('#requestDiv').show();
        $('#preRequestDiv').hide();
        $('#preSaveDiv').hide();
        $('#saveDiv').hide();
    } else {
        $('#requestDiv').hide();
        $('#preRequestDiv').hide();
        $('#preSaveDiv').hide();
        $('#saveDiv').show();
    }
}
