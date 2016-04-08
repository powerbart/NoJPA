package dk.lessismore.nojpa.reflection.translate;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public interface TranslateService {


    public String translate(String uniqueID, String attributeNameForDebug, String srcLang2Char, String destLang2Char, String originalText) throws Exception;


}
