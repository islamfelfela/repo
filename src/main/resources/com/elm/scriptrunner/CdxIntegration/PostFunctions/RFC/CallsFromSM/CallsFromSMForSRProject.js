//                                                     ========= CAB Approval ---> Under Implementation ========

function appendLeadingZeroes(n){
if(n <= 9){
return "0" + n;
}
return n
}
var JSON = system.library.JSON.json();

var approvalTable= new SCFile("Approval");
var approvalQuery = "unique.key = \""+ record.number+ "\"";
var approval_rc=approvalTable.doSelect(approvalQuery );
var ChangeApprovalComments = "Approved - Implementation In Progress - "+ system.library.JSON2.toJSON(approvalTable.comments.toArray().join(' ')).replace(/['"]+/g, '');
//print(ChangeApprovalComments)
var headers = new Array();
headers.push(new Header( "Content-Type", "application/json" ))
headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
var respHeaders = new Object();
var data = '{"update": {"comment": [{"add": {"body": "'+ChangeApprovalComments+'"}}]},"transition":{"id": "51"},"fields":{"customfield_15201": "'+plan_start+'","customfield_15202": "'+plan_end+'"}}'
print(data);
var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jira_id+'/transitions?expand=transitions.fields';
var response = doHTTPRequest("POST", url, headers, data, 10, 10, null, respHeaders);

//                                                     ========= CAB Approval ---> Request More Information ========

record.elm_jira_status = "Request More information";
var JSON = system.library.JSON.json();
var approvalTable= new SCFile("Approval");
var approvalQuery = "unique.key = \""+ record.number+ "\"";
var approval_rc=approvalTable.doSelect(approvalQuery );
print(system.library.JSON2.toJSON(approvalTable.comments.toArray().join(' ')));
print(record.elm_jira_id);
var ChangeRejectionReason = "Request More Information - "+ system.library.JSON2.toJSON(approvalTable.comments.toArray().join(' ')).replace(/['"]+/g, '');
var headers = new Array();
headers.push(new Header( "Content-Type", "application/json" ))
headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
var respHeaders = new Object();
// transition updated from 41 to 271
var data = '{"update": {"comment": [{"add": {"body": "'+ChangeRejectionReason+'"}}]},"transition":{"id": "31"}}'
var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jira_id+'/transitions?expand=transitions.fields';
var response = doHTTPRequest("POST", url, headers, data, 10, 10, null, respHeaders);

//                                                         ========= Under Implementation ---> Change Closure=======

var JSON = system.library.JSON.json();
var PostImplementationComments = record.elm_chm_progress_status;
if( record.elm_chm_progress_status == "Implementation Failed" ){
    PostImplementationComments= PostImplementationComments + " with failed caused: " + record.elm_chm_failed_causes;
    if(record.elm_chm_issues_details != null ){
        PostImplementationComments= PostImplementationComments + " & Issues details: " + system.library.JSON2.toJSON(record.elm_chm_issues_details.toArray().join(' ')).replace(/['"]+/g, '');
    }
}
if( record.elm_chm_progress_status == "Implementation Failed" ){
var JiraImpStatus = "Fail";
}
else if ( record.elm_chm_progress_status == "Implementation Completed" ){
var JiraImpStatus = "Pass";
}
var headers = new Array();
headers.push(new Header( "Content-Type", "application/json" ))
headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
var respHeaders = new Object();
// transition updated from 51 to 541
var data = '{"update": {"comment": [{"add": {"body": "'+PostImplementationComments+'"}}]},"transition":{"id": "61"},"fields":{"resolution":{"name":"'+JiraImpStatus +'"}}}'
//print(data);
var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jira_id+'/transitions?expand=transitions.fields';
var response = doHTTPRequest("POST", url, headers, data, 10, 10, null, respHeaders);