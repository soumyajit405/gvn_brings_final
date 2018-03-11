
<!DOCTYPE html >
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>BORAJI.COM</title>
<!-- CSS for Bootstrap -->
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
<!-- JQuery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

<script type="text/javascript">
$(function() {
  $('button[type=submit]').click(function(e) {
    e.preventDefault();
    //Disable submit button
    $(this).prop('disabled',true);
    
    var form = document.forms[0];
    var formData = new FormData(form);
    	
    // Ajax call for file uploaling
    var ajaxReq = $.ajax({
      url : 'http://brings.co.in/brings/rest/completeTrip',
      type : 'POST',
      data : formData,
      cache : false,
      contentType : false,
      processData : false,
      xhr: function(){
        //Get XmlHttpRequest object
         var xhr = $.ajaxSettings.xhr() ;
        
        //Set onprogress event handler 
        
         return xhr ;
    	},
    	beforeSend: function( xhr ) {
    		//Reset alert message and progress bar
    	
              }
    });
  
    // Called on success of file upload
    ajaxReq.done(function(msg) {
    	console.log(msg);
     
    });
    
    // Called on failure of file upload
    ajaxReq.fail(function(jqXHR) {
    
    });
  });
});
</script>

</head>
<body>
  <div class="container">
    <h2>Spring MVC - File Upload Example With Progress Bar</h2>
    <hr>
    <!-- File Upload From -->
    <form action="fileUpload" method="post" enctype="multipart/form-data">
      <div class="form-group">
        <label>Select File</label> 
        <input class="form-control" type="file" name="orderPic">
       <!--  <input class="form-control" type="file" name="vehiclerc">
        <input class="form-control" type="file" name="drivinglic">
        <input class="form-control" type="file" name="pvin">
         <input class="form-control" type="file" name="image"> -->
        <input class="form-control" type="email" name="email">
        <input class="form-control" type="text" name="orderId">
        <input class="form-control" type="text" name="deliveryCode">
      </div>
      <div class="form-group">
        <button class="btn btn-primary" type="submit">Upload</button>
      </div>
    </form>
    <br />

    <!-- Bootstrap Progress bar -->
    <div class="progress">
      <div id="progressBar" class="progress-bar progress-bar-success" role="progressbar"
        aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">0%</div>
    </div>

    <!-- Alert -->
    <div id="alertMsg" style="color: red;font-size: 18px;"></div>
  </div>
</body>
</html>