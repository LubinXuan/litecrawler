package us.codecraft.webmagic.selector;

import org.jsoup.select.Elements;

/**
 * Created by Lubin.Xuan on 2017-10-25.
 * {desc}
 */
public class HtmlNodeUtils {

    public static Elements from(Selectable selectable) {
        if (selectable instanceof HtmlNode) {
            return new Elements(((HtmlNode) selectable).getElements());
        } else {
            return new Elements();
        }
    }

}
