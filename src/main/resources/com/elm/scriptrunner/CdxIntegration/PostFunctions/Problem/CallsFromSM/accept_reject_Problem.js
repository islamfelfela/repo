//
//
//
//
//                                           ==============================Accept ==============================
//var headers = new Array();
//headers.push(new Header( "Content-Type", "application/json" ))
//headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
//var respHeaders = new Object();
////var data = '{"fields":{"resolution":{"name":"Done"} },"transition":{"id": "241"}}';
//var data = '{"transition":{"id": "241"}}';
//var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jazz_defect_number+'/transitions?expand=transitions.fields';
//var response = doHTTPRequest( "POST", url, headers, data , 10, 10, null, respHeaders );
// print(data)
// print(response)
//
//=====Reject=====
//var problemRejectionReason = system.vars.$pm_activity + " - "+system.vars.$rc_update.toArray();
//
//var headers = new Array();
//headers.push(new Header( "Content-Type", "application/json" ))
//headers.push(new Header( "Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw==" ))
//var respHeaders = new Object();
//var data = '{"fields":{"customfield_12005" :'+problemRejectionReason+'},"transition":{"id": "251"}}';
//var url = 'http://10.33.6.16:8080/rest/api/2/issue/'+record.elm_jazz_defect_number+'/transitions?expand=transitions.fields';
//var response = doHTTPRequest( "POST", url, headers, data , 10, 10, null, respHeaders );
// print(data)
// print(response)
