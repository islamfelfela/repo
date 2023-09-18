//
//                                                     ========= Update Incident on JIRA ========

// var incident_status =

// print(ChangeApprovalComments)
// var headers = new Array();
// headers.push(new Header( "Content-Type", "application/json" ))
// headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
// var respHeaders = new Object();
// var data = '{"update": {"comment": [{"add": {"body": "'+comment+'"}}]},"fields":{"customfield_14620": "'+incident_status+'"}}'
// print(data)
// var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jira_id+'/';
// var response = doHTTPRequest("POST", url, headers, data, 10, 10, null, respHeaders);
// print(response)
//


//    //print(record.problem_status);
////print(record.resolution);
////print(record.elm_jira_id);
//// var incident_status =
// //print(ChangeApprovalComments);
//var JSON = system.library.JSON.json();
//var description  = system.library.JSON2.toJSON(record.resolution.toArray().join(' ')).replace(/['"]+/g, '')
////print(description);
//var headers = new Array();
//headers.push(new Header( "Content-Type", "application/json" ))
//headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
//var respHeaders = new Object();
//var data = '{"update": {"comment": [{"add": {"body": "Status : '+record.problem_status+' \n\r Description : '+description+'"}}]}}'
//print(data);
//var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jira_id+'/';
//var response = doHTTPRequest("PUT", url, headers, data, 10, 10, null, respHeaders);
//print(response);
