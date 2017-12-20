package search.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import main.SearchManager;
import search.FileHandler;
import search.Parser;
import search.WebSpider;
import vo.Program;

/**
 * 请在此类中完成自己对象初始化工作，并注册
 */

public class Manager {
	
	
	
    static{
    	
        // TODO:在此初始化所需组件，并将其注册到SearchManager中供主函数使用
        // SearchManager.registFileHandler(new yourFileHandler());
        // SearchManager.registSpider(new yourSpider());
    	SearchManager.registSpider(new WebSpider() {
			@Override
			public Parser getParser() {
				// TODO Auto-generated method stub
				Parser parser=new Parser() {
					@Override
					public Program parseHtml(String html) {
						Program temp=new Program();
						Document document=null;
						Connection connection=Jsoup.connect(html);
						try {
							document=connection.get();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						org.jsoup.nodes.Element elem =document.select("div#AllContents").first();
						// TODO Auto-generated method stub
						temp.setCountry("America");
						temp.setDeadlineWithAid(null);
						temp.setSchool(elem.children().select("span").first().text());
						temp.setDegree(elem.children().select("h1").first().child(1).text());
						if (document.select("h4:contains(Application Deadline:)").first()!=null) {
							if(document.select("h4:contains(Application Deadline:)").first().nextElementSibling().text().contains("funding")) {
								Element element2=document.select("h4:contains(Application Deadline:)").first().nextElementSibling().children().select("em:contains(funding)").first();
								temp.setDeadlineWithAid(element2.previousElementSibling().text()+" "+element2.text());
								temp.setDeadlineWithoutAid(document.select("h4:contains(Application Deadline:)").first().nextElementSibling().text().replaceAll(element2.previousElementSibling().text(),"").replaceAll(element2.text(), ""));
							}
							else temp.setDeadlineWithoutAid(document.select("h4:contains(Application Deadline:)").first().nextElementSibling().text());
						}
						else temp.setDeadlineWithoutAid(null);
						temp.setHomepage(html);
						temp.setUniversity("University of Delaware");
						Element element=null;
						Elements elements =document.select("a[href]");
						for (Element e:elements) {
							if (e.attr("href").contains("mailto")) {
								element=e;
							break;
							}
						}
						temp.setPhoneNumber(element.previousSibling().outerHtml().replaceAll("•", ""));
						temp.setEmail(element.attr("href"));
						temp.setLocation(element.parent().text().replace(element.previousSibling().outerHtml(),"").replaceAll(element.text(), ""));
						temp.setProgramName(elem.children().select("h1").first().child(0).text());
						String id=UUID.randomUUID().toString().replaceAll("-", "");
						temp.setId(id);
						return temp;
					}
				};
				return parser;
			}
			
			@Override
			public List<String> getHtmlFromWeb()  {
				// TODO Auto-generated method stub
				//获取主页
				List<String>htmls=new LinkedList<>();
				String url ="http://grad.udel.edu/graduate-programs/?frm-page-2654=";
				for (int i=1;i<=3;i++) {
					Connection connection=Jsoup.connect(url+i);
					Document document=null;
					try {
						document= connection.get();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							}
					org.jsoup.select.Elements elements=
							document.getElementsByAttributeValue("class", "button-primarygold5");
					for(org.jsoup.nodes.Element elem:elements){
						if (elem.text().contains("Program Details")) {
							String str=elem.attr("href");
							htmls.add(str);
							}
						}
					}
				return htmls;
			}
		});
    	SearchManager.registFileHandler(new FileHandler() {
			
			@Override
			public int program2File(List<Program> programList) {
				// TODO Auto-generated method stub
				int count =0;
				File writeFile =new File("./output/University of Delaware.txt");
				BufferedWriter writer=null;
				try {
					writer=new BufferedWriter(new FileWriter(writeFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (Program program:programList) {
					if (program.getDeadlineWithoutAid()!=null)
					try {
						writer.write(
								program.getUniversity()+"\t"
								+program.getCountry()+"\t"
								+program.getProgramName()+"\t"
								+program.getSchool()+"\t"
								+program.getDegree()+"\t"
								+program.getEmail()+"\t"
								+program.getPhoneNumber()+"\t"
								+program.getLocation()+"\t"
								+program.getDeadlineWithAid()+"\t"
								+program.getDeadlineWithoutAid()+"\t"
								+program.getHomepage()+"\n");
						count ++;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return count;
			}
		});
    }
}
