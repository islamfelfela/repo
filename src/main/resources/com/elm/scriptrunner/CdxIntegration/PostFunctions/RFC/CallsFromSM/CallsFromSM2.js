function appendLeadingZeroes(n) {
if (n <= 9) {
return "0" + n;
}
return n
}
var JSON = system.library.JSON.json();
var outage_exits = "No";
var outage_exits = record.elm_chm_outage;
print("outage_exits :" + outage_exits);

function formatDateTime(date_value)
{

var formatteddate = date_value.getFullYear() + "-" + appendLeadingZeroes(date_value.getMonth() + 1) + "-" + appendLeadingZeroes(date_value.getDate()) + "T" + appendLeadingZeroes(date_value.getHours() - 3) + ":" + appendLeadingZeroes(date_value.getMinutes()) + ":" + appendLeadingZeroes(date_value.getSeconds()) + ".000+0000"

return(formatteddate);
}

var start_datetime = new Date(record.planned_start);
var end_datetime = new Date(record.planned_end);
var plan_start = formatDateTime(start_datetime);
var plan_end = formatDateTime(end_datetime);
if (outage_exits == "Yes")
{
var down_start = new Date(record.elm_chm_down_start);
var down_end = new Date(record.elm_chm_down_end);
var outage_start = formatDateTime(down_start);
var outage_end = formatDateTime(down_end);
}
var approvalTable = new SCFile("Approval");
var approvalQuery = "unique.key = \"" + record.number + "\"";
var approval_rc = approvalTable.doSelect(approvalQuery);
//print(system.library.JSON2.toJSON(approvalTable.comments.toArray().join(' ')));
var ChangeApprovalComments = "Approved - Implementation In Progress - " + system.library.JSON2.toJSON(approvalTable.comments.toArray().join(' ')).replace(/['"]+/g, '');
//print(ChangeApprovalComments)
var headers = new Array();
headers.push(new Header("Content-Type", "application/json"))
headers.push(new Header("Authorization", "Basic aW5naHBzbTpFTE1ocHNtQDEyMw=="))
var respHeaders = new Object();
// transition updated from 61 to 251
//var data = '{"update": {"comment": [{"add": {"body": "'+ChangeApprovalComments+'"}}]},"transition":{"id": "251"},"fields":{"customfield_15201": "'+plan_start+'","customfield_15202": "'+plan_end+'"}}'
if (record.elm_service_environment == "cdx") {
if (outage_exits == "Yes") {
var data = '{"update": {"comment": [{"add": {"body": "' + ChangeApprovalComments + '"}}]},"transition":{"id": "501"},"fields":{"customfield_15201": "' + plan_start + '","customfield_15202": "' + plan_end + '" ,"customfield_15304":{"value":"' + outage_exits + '"},"customfield_15302":"' + outage_start + '" ,"customfield_15303":"' + outage_end + '"}}'
}
else
{
var data = '{"update": {"comment": [{"add": {"body": "' + ChangeApprovalComments + '"}}]},"transition":{"id": "501"},"fields":{"customfield_15201": "' + plan_start + '","customfield_15202": "' + plan_end + '" ,"customfield_15304":{"value":"No"}}}'}
}
else
{
if (outage_exits == "Yes")
{
var outage_type = record.elm_outage_type; //
if (outage_type == "Full")
{
var data = '{"update": {"comment": [{"add": {"body": "' + ChangeApprovalComments + '"}}]},"transition":{"id": "251"},"fields":{"customfield_15201": "' + plan_start + '","customfield_15202": "' + plan_end + '" ,"customfield_15304":{"value":"' + outage_exits + '"},"customfield_15302":"' + outage_start + '" ,"customfield_15303":"' + outage_end + '","customfield_15914":{"value":"Full"}}}'
}
else
{
var outage_start1 = new Date(record.elm_outage_start1);
var outage_start2 = new Date(record.elm_outage_start2);
var outage_start3 = new Date(record.elm_outage_start3);
var outage_start4 = new Date(record.elm_outage_start4);
var outage_end1 = new Date(record.elm_outage_end1);
var outage_end2 = new Date(record.elm_outage_end2);
var outage_end3 = new Date(record.elm_outage_end3);
var outage_end4 = new Date(record.elm_outage_end4);
var plan_start1 = formatDateTime(outage_start1);
var plan_end1 = formatDateTime(outage_end1);
var plan_start2 = formatDateTime(outage_start2);
var plan_end2 = formatDateTime(outage_end2);
var plan_start3 = formatDateTime(outage_start3);
var plan_end3 = formatDateTime(outage_end3);
var plan_start4 = formatDateTime(outage_start4);
var plan_end4 = formatDateTime(outage_end4);
var data = '{"update": {"comment": [{"add": {"body": "'+ChangeApprovalComments+'"}}]},"transition":{"id": "251"},"fields":{"customfield_15201": "' + plan_start +'","customfield_15202": "' + plan_end + '" ,"customfield_15304":{"value":"' + outage_exits + '"} ,"customfield_15914":{"value":"Interruption"}, "customfield_15302":"' + outage_start + '" ,"customfield_15303":"' + outage_end + '", "customfield_15920": "' + plan_start1 + '","customfield_15921": "' + plan_start2 + '" ,"customfield_15922": "' + plan_start3 + '" ,"customfield_15923": "' + plan_start4 + '" ,"customfield_15915": "' + plan_end1 + '","customfield_15917": "' + plan_end3 + '" ,"customfield_15918": "' + plan_end2 + '" ,"customfield_15919": "' + plan_end4 + '"}}'}
}
else {
var data = '{"update": {"comment": [{"add": {"body": "' + ChangeApprovalComments + '"}}]},"transition":{"id": "251"},"fields":{"customfield_15201": "' + plan_start + '","customfield_15202": "' + plan_end + '" ,"customfield_15304":{"value":"No"}}}'
}
}
print(data);
var url = 'http://10.33.6.16:8080/rest/api/2/issue/' + record.elm_jira_id + '/transitions?expand=transitions.fields';
var response = doHTTPRequest("POST", url, headers, data, 10, 10, null, respHeaders);
print(response);