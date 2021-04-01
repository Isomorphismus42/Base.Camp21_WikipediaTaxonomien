package basecamp.taxonomien.springwebserver;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Steuert die Startseite
 */
@Controller
public class HomeController {

    public HomeController() {
        initText();
    }

    private String hellowWorldMessage;
    private String[] infoTexts = new String[3];
    private String[] headings = new String[3];

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("message",hellowWorldMessage);
        model.addAttribute("headings",headings);
        model.addAttribute("infoTexts",infoTexts);
        return "home";
    }

    private void initText() {
        hellowWorldMessage  = "This application visualizes taxonomies that were found by scanning a " +
                "Wikipedia data dump with Hadoop and MapReduce.";
        headings = new String[] {"Data Source", "Data Processing", "Data Visualization"};
        infoTexts[0] = "We used the official English Wikipedia dump file released on the 22nd February 2021. " +
                "The initial file size was around 80 GB. We could reduce it to 14 GB after removing the XML formatting " +
                "using WikiExtractor.";
        infoTexts[1] = "For finding taxonomies we developed a couple of regular expressions and matched them against the text. " +
                "For increased precision and better results, the data was edited with Apache OpenNLP, utilising the " +
                "SentenceDetector, Tokenizer, Chunker, Lemmatizer and POS Tagger models.";
        infoTexts[2] = "To visualize the data we use the tree layout provided by D3. We build tree graphs by " +
                "assigning every found parent-child pair a value based on the quantity and quality of the patterns " +
                "that found it and choose the highest valued pair.";
    }
}
