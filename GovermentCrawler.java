package ai.zhuanzhi.crawler.impl.news.goverment;
import ai.zhuanzhi.crawler.core.CommonZhuanzhiCrawler;
import cn.edu.hfut.dmic.contentextractor.ContentExtractor;
import cn.edu.hfut.dmic.contentextractor.News;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import ai.zhuanzhi.crawler.model.Article;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GovermentCrawler extends CommonZhuanzhiCrawler {
    public static  int i = 0;
    public GovermentCrawler() {
//        String[]   channelNos = {"103","22","106","104","21","105","111",
//                "112","107","4","102","110","2"};
//        for (String channelNo : channelNos) {
//            String url = "https://www.huxiu.com";
//            url = url + "/channel/" + channelNo + ".html";
//            addSeedAndReturn(url).type("seed");
//        }
        setTest(true);
        setRequester(new OkHttpRequester());
        addSeedAndReturn("https://www.huxiu.com/tags.html").type("seed");
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.matchType("seed")) {
            Elements links = page.select("a[href]");
            for(Element link:links) {
                Pattern p = Pattern.compile("https://www.huxiu.com/tags/[0-9]+.html");
                Matcher m = p.matcher(link.absUrl("href"));
                if(m.find()){
                    next.addAndReturn(m.group(0));
                }
            }
        }
        else if (page.matchUrl("https://www.huxiu.com/tags/[0-9]+.html")){
            Elements links = page.select("a[href]");
            for(Element link:links){
                next.addAndReturn(link.absUrl("href")).meta("title",link.text());;
            }
        }
        else {
            if(page.matchUrl("https://www.huxiu.com/article/[0-9]+.html")){
                try {
                    Article article = getArticleFromPage(page);
                    ackContent(page.url(),article);
                    System.out.println(page.meta("title") + "##" + String.valueOf(i++));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public String getTaskId() { return GovermentConfig.taskId; }
    public Article getArticleFromPage(Page page) throws Exception {
        Article article = null;
        if (page.url().contains("news.sina.com.cn")){
            Element articleElement = page.select(".article").first();
            article = new Article(getTaskId(),page.url(),page.meta("title"),
                    articleElement.outerHtml(),true,"佚名","",0
                    , DateTime.now().getMillis(),null,null,0,null, GovermentConfig.taskType);
        }else {
            News news = ContentExtractor.getNewsByDoc(page.doc());
            article = new Article(getTaskId(),page.url(),news.getTitle(),
                    news.getContentElement().outerHtml(),true,"佚名","",0
                    , DateTime.now().getMillis(),null,null,0,null,GovermentConfig.taskType);
        }
        return article;
    }
    public static void main(String[] args) throws Exception {
        GovermentCrawler crawler = new GovermentCrawler();
        crawler.start(20);
//        crawler.start(Integer.MAX_VALUE);
    }
}
