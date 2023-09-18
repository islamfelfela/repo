package com.elm.scriptrunner.AnnouncementService

import com.elm.scriptrunner.library.CommonUtil
import groovy.xml.MarkupBuilder


def writer = new StringWriter()
def html = new MarkupBuilder(writer)

html.style(type: "text/css",
        '''
                    #myTable, th, td{
                    border: 1px solid white;
                    padding: 2px;
                    text-align: center;
                    border-collapse: collapse;
                    },
                    #txt{
                    font-size:21px;
                    line-height:105%;
                    font-family:"DIN Next LT Arabic";
                    sans-serif;
                    color:#026CB6;
                    }
            ''')
html.table('id': 'myTable') {
    tr('id': 'txt') {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('Planned Service Outage Announcement')
        }
        td(style: 'width:10%') {

        }
        td(style: 'text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', 'تنبيه مجدول بتوقف الخدمة')
        }

    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('Dear Customer,')
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', 'عزيزي العميل،')
        }
    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('Out of our efforts to enhance our services, we would like to inform you that there will be an outage for the service, as below:')
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', ': نعمل على تطوير أنظمتنا لخدمتكم بشكل أفضل، ونفيدكم بأن خدماتنا ستكون غير متاحة حسب التفاصيل أدناه')
        }
    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('Planned Outage Timetable:')
            table() {
                tr {
                    td('Start Time')
                    td('09/11/22 12:00')
                }
                tr {
                    td('End Time')
                    td('09/11/22 12:00')
                }
                tr {
                    td('Duration')
                    td('1')
                }
            }
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', ': الجدول الزمني للتوقف')
            table() {
                tr {
                    td(style: '''dir:RTL''', 'وقت بدء التوقف :')
                    td('09/11/22 12:00')
                }
                tr {
                    td(style: '''dir:RTL''', 'الوقت المتوقع لانتهاء التوقف :')
                    td('09/11/22 12:00')
                }
                tr {
                    td(style: '''dir:RTL''', 'المده :')
                    td('1')
                }
            }
        }

    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('If you have any question, or if you continue to experience any problems after the above period,please contact us via :')
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', 'إذا كان لديكم أي استفسارات، أو واجهتكم مشكلة بعد الفترة المذكورة أعلاه، يسعدنا تواصلكم معنا عبر القنوات التالية :')
        }
    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            table{
                tr {
                    td('Phone:')
                    td('         ')
                    td('920000356')
                }
                tr{
                    td('Email:')
                    td('         ')
                    td('hd@elm.sa')
                }
            }
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            table{
                tr {
                    td('الهاتف:')
                    td('         ')
                    td('920000356')
                }
                tr{
                    td('البريد الإلكتروني:')
                    td('         ')
                    td('hd@elm.sa')
                }
            }
        }
    }
    tr {
        td(style: 'text-align:left;border-collapse:collapse; width:45%') {
            p('We apologize for this inconvenience and thank you for your understanding.')
        }
        td(style: 'width:10%') {

        }
        td(style: 'dir:RTL;text-align:right;border-collapse:collapse; width:45%') {
            p(style: '''dir:RTL''', 'شكرًا لتفهمكم، ونعتذر عن أي إزعاج قد يترتب على ذلك.')
        }
    }
}
String finalHTML = writer.toString()
