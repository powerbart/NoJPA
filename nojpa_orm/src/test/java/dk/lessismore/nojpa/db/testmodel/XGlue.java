package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.IndexField;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by seb on 19/07/16.
 */
public interface XGlue extends ModelObjectInterface {


    @SearchField
    int getSentenceNumber();
    void setSentenceNumber(int sentenceNumber);


    // TODO multiple glues with the same entity+article... test what it does
    @SearchField
    XNlpScore getNlpScoreA();
    void setNlpScoreA(XNlpScore score);

    @SearchField
    XNlpScore getNlpScoreB();
    void setNlpScoreB(XNlpScore score);

    @SearchField
    XNlpScore getNlpScoreC();
    void setNlpScoreC(XNlpScore score);

    @SearchField
    XNlpScore getNlpScoreD();
    void setNlpScoreD(XNlpScore score);

//    // nlp matrices
//    @SearchField
//    XNlpValue getBranchNlp();
//    void setBranchNlp(XNlpValue branchNlp);
//
//    @SearchField
//    XNlpValue getSentenceNlp();
//    void setSentenceNlp(XNlpValue sentenceNlp);
//
//    XNlpValue getPrevSentenceNlp();
//    void setPrevSentenceNlp(XNlpValue prevSentenceNlp);
//
//    XNlpValue getNextSentenceNlp();
//    void setNextSentenceNlp(XNlpValue nextSentenceNlp);




}
