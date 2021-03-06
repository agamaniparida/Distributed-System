window.pagingIncluded = true;
window.jsonObjs = [];


function success(res) {
  
	if (res.stories) {		
		prs = res;
	} else {
		prs = jQuery.parseJSON( res );				
	}

  	tagsFilterParam = getUrlParameter('tags');
  
	if ( tagsFilterParam != null ) {
    	tagsFilter = tagsFilterParam.toLowerCase();
		filteredArr = [];
		//console.log("loop over stories looking for " + tagsFilter);	
     	$('#message-'+prs.id).html('<h3>Articles tagged: <strong>'+tagsFilter+'</strong></h3><span class="small"><a href="?">clear filter</a></span>');
		for ( var i = 0; i < prs.stories.length; i ++ ) {
        	tagsArr = prs.stories[i].tags;
			if ( tagsArr.indexOf(tagsFilter) > -1 ) {
				//console.log(tagsArr);
				filteredArr.push(prs.stories[i]);
			}
		}
		prs.stories = filteredArr;
		autoLoadFirstPage=true;
	}
  
	
	jsonObjs[prs.id] = [];
	jsonObjs[prs.id].stories = prs.stories;
	
	jsonObjs[prs.id].numRows = prs.stories.length;
	jsonObjs[prs.id].rowspp = prs.rowspp;
	numpages = Math.ceil( jsonObjs[prs.id].numRows / jsonObjs[prs.id].rowspp );
    if (numpages > 1) {
		for (i = 1; i <= numpages ; i++) {
			$('#paging-'+prs.id).append('<li id="m'+prs.id+'-'+i+'"><a href="javascript:gotopage('+i+','+prs.id+')">'+i+'</a></li>');
		}
   }
   
   
   cname = 'page-'+prs.id;
   lastPage = '';//readCookie(cname);

   if (lastPage) {
   	 gotopage(lastPage,prs.id,true); 
   } else if ( typeof autoLoadFirstPage !== 'undefined' && autoLoadFirstPage) {
   	 gotopage(1,prs.id,true); 
   } else {
		$('#m'+prs.id+'-1').addClass('active');	
   }
}

function gotopage(num,c,skipAnimation) {
	 var skipAnimation = typeof skipAnimation !== 'undefined' ?  skipAnimation : false;
	$('#paging-'+c+' li').removeClass('active');	
	$('#m'+c+'-'+num).addClass('active');	
	startRow = (num-1)*jsonObjs[c].rowspp+1;

	endRow = startRow + parseInt(jsonObjs[c].rowspp) - 1;
	if (endRow > jsonObjs[c].numRows) 
		endRow = jsonObjs[c].numRows;
	//if ( jsonObjs[c].numRows > endRow)
	//$('#ct-'+c).html('Showing rows '+startRow+' to ' + endRow + ' of ' + jsonObjs[c].numRows );
  
	elmt = $('.pagedrows-'+c);
	if ( skipAnimation) {
			allhtm = '';
			
			for (r = startRow-1; r < endRow ; r++) {
				allhtm += jsonObjs[c].stories[r].out;
			}
		   elmt.html(allhtm);
		   elmt.show();
	} else {
	
		elmt.slideUp(300,function() {
			allhtm = '';
			
			for (r = startRow-1; r < endRow ; r++) {
				allhtm += jsonObjs[c].stories[r].out;
			}
		   elmt.html(allhtm);
			elmt.slideDown(300);
			
		})
	}
	
	cname = 'page-'+c;
	eraseCookie(cname);
	createCookie(cname,num,15); 
	
}

function loadJSON(j) {
	$.ajax({  url: j,  success: success }).fail(function() { alert("Failed to load json [3]");});
}


function getUrlParameter(sParam) {
  
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
	return null;
};


function createCookie(name, value, minutes) {
    var expires;
    if (minutes) {
        var date = new Date();
        date.setTime(date.getTime() + ( minutes * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    } else {
        expires = "";
    }
    document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = encodeURIComponent(name) + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
    return null;
}

function eraseCookie(name) {
    createCookie(name, "", -1);
}



