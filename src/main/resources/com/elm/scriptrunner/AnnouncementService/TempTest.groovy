static def plannedServiceOutageAnnouncementInHtmlFormat(Map m, def serviceList ) {
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)

    html.style(type: "text/css",
            '''
					#myTable{
						border: 1px solid white;
						align: center;
						border-collapse: collapse;
					}
					#txt{
						font-size:21px;
						line-height:105%;
						font-family:"DIN Next LT Arabic";
						sans-serif;
						color:#026CB6;
					}
					#txtParagraph{
						font-size:14px;
						line-height:105%;
						font-family:"DIN Next LT Arabic";
						sans-serif;
					}
					#txtParagraphColored{
						font-size:14px;
						line-height:105%;
						font-family:"DIN Next LT Arabic";
						sans-serif;
						background-color:#BDD6EE;
					}
					#internalLeftTable{
						border: 1px solid ;
						padding: 1px;
						text-align: left;
						height: 180px
					}
					#internalRightTable{
						border: 1px solid ;
						padding: 1px;
						text-align: right;
						height: 180px;
					}
					th, td{
					  padding: 2px;
				}
				  th{
						background-color: #04AA6D;
						color: white;
				}
				#leftText45{
						text-align:left;
						border-collapse:collapse;
				}
				#rightText45{
						text-align:right;
						border-collapse:collapse;
						dir:RTL;
				}
			''')
    html.div(style:'overflow-x:auto;') {
        table('id': 'myTable') {
            tr('id': 'txt') {
                td('id': 'leftText45') {
                    p('Unplanned Service Outage Announcement')
                }
                td('id': 'rightText45') {
                    p('إعلان توقف الخدمة المفاجئ')
                }

            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Dear Customer,')
                }
                td('id': 'rightText45') {
                    p('عزيزي العميل،')
                }
            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Due to the temporarily Unplanned Outage on Service occurred, which is out of our control, we are working on restoring the service to normal. We will notify you when the service is restored.')
                }
                td('id': 'rightText45') {
                    p('نظرًا للانقطاع المؤقت والخارج عن إرادتنا لخدمة المقدمة والتي يجري العمل الآن لإعادتها مجددا فإننا نعتذر لكم عن ذلك وسيتم إشعاركم حال عودة الخدمة للعمل.')
                }
            }
            tr {
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:left; border-collapse:collapse; width:20%') {
                                p('Affected services')
                            }
                            td {
                                table('id': 'internalLeftTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
												def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
												String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
												String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Name')?.first()?.toString()
												String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Name')?.first()?.toString() : ''
												tr {
													td(serviceEnCol1)
													td(serviceEnCol2)
													td(serviceEnCol3)
													i += 3
												}
											}
										} else {
											def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:right; border-collapse:collapse; width:45%; dir:RTL;') {
                                table('id': 'internalRightTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
												def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
												String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
												String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Service Name AR')?.first()?.toString()
												String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Service Name AR')?.first()?.toString() : ''
												tr {
													td(serviceEnCol1)
													td(serviceEnCol2)
													td(serviceEnCol3)
													i += 3
												}
											}
										} else {
											def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                                td(style: 'text-align:right; border-collapse:collapse; width:10%; dir:RTL;') {
                                    p('الخدمات المتأثره')
                                }
                            }
                        }
                    }
                }
            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    p('If you have any question, or if you continue to experience any problems after the above period,please contact us via :')
//                }
//                td('id': 'rightText45') {
//                    p(': إذا كان لديكم أي استفسارات، أو واجهتكم مشكلة بعد الفترة المذكورة أعلاه، يسعدنا تواصلكم معنا عبر القنوات التالية')
//                }
//            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:left;', 'Phone:')
//                            td('920000356')
//                        }
//                        tr {
//                            td(style: 'text-align:left;', 'Email:')
//                            td('hd@elm.sa')
//                        }
//                    }
//                }
//                td('id': 'rightText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:right;', '920000356')
//                            td(style: 'text-align:right;', ': الهاتف')
//                        }
//                        tr {
//                            td(style: 'text-align:right;', 'hd@elm.sa')
//                            td(style: 'text-align:right;', ': البريد الإلكتروني')
//                        }
//                    }
//                }
//            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('We apologize for this inconvenience and thank you for your understanding.')
                }
                td('id': 'rightText45') {
                    p('شكرًا لتفهمكم، ونعتذر عن أي إزعاج قد يترتب على ذلك.')
                }
            }
        }
    }

    return (writer.toString())
}