package com.adamyi.courseplanner.nlp;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnalyzeSyntaxRequest;
import com.google.api.services.language.v1.model.AnalyzeSyntaxResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Token;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class NLPTask {

    private static final Logger Log = Logger.getLogger(NLPTask.class.getName());

    private String mMessage;

    private ParseTree mParseTree;
    private GoogleCredential mCredential;

    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).setApplicationName("MessageOnTap").build();

    public NLPTask(String message) {
        this.mMessage = message;
    }

    // *******************
    private String getAccessToken()  {
        File initialFile = new File("/opt/credentials.json");
        try {
            InputStream stream = new FileInputStream(initialFile);
            final GoogleCredential credential = GoogleCredential.fromStream(stream)
                    .createScoped(CloudNaturalLanguageScopes.all());
            credential.refreshToken();
            final String accessToken = credential.getAccessToken();
            return accessToken;
        } catch (IOException e) {
            Log.info("GoogleToken Failed to obtain access token." + e.toString());
        }
        return null;
    }


    public void run() {
        mParseTree = new ParseTree();
        mCredential = new GoogleCredential().setAccessToken(getAccessToken());
        try {
            Log.info("CurrentMessage " + mMessage);
            preprocessMessage();
            buildTree();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preprocessMessage() {
        mMessage.replaceAll("[pP]rogram \\d{4}", "PRGM$1");
        mMessage.replaceAll("Bachelor of Science", "BS");
        mMessage.replaceAll("BS \\(Honours\\)", "BSH");
        mMessage.replaceAll("BS \\(Hon\\)", "BSH");
        mMessage.replaceAll("Bachelor of Engineering", "BE");
        mMessage.replaceAll("BE \\(Honours\\)", "BEH");
        mMessage.replaceAll("BE \\(Hon\\)", "BEH");
    }

    private void buildTree() {
        try {

            constructTree(mApi
                    .documents().analyzeSyntax(new AnalyzeSyntaxRequest().setDocument(
                            new Document().setContent(mMessage).setType("PLAIN_TEXT"))
                    ).execute());
            processTree();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTree() {
        mParseTree.merge();
        TreeParser tp = new TreeParser(mParseTree);

//        for(int i = 0; i < mTags.size(); i++) {
//            int key = mTags.keyAt(i);
//            // get the object by the key.
//            SemanticTag obj = mTags.get(key);
//        }

        //tp.addTag(mTags);

        //TODO: implement these.
        tp.analyzeRequisite();
        tp.analyzeCourses();
        tp.reduceTree();

        mParseTree = tp.getTree();
        Log.info("Tree " + mParseTree.toString());
    }

    /**
     * Transform the analyzed syntax result into a dependency parse tree,
     * merge the semantically inseparable words.
     * @param response the analyzed syntax result from
     *                 Google Natural Language Understanding Services.
     */
    private void constructTree(AnalyzeSyntaxResponse response) {
        Log.info("STATUS constructTree");
        List<Token> tokens = response.getTokens();
        for (int i = tokens.size() - 1; i > -1; --i) {
            Token token = tokens.get(i);
            ParseTree.Node node = mParseTree.getNodeById(i);
            if (node == null)
                node = new ParseTree.Node();

            node.setValue(token.getPartOfSpeech().getTag(), token.getText().getContent());
            node.setRelation(token.getDependencyEdge().getLabel());
            if (StringUtils.equals(node.getRelation(), ParseTree.DEP_NOUN_NOUN)
                    || StringUtils.equals(node.getRelation(), ParseTree.DEP_GOES_WITH))
                node.setFlag(ParseTree.Flag.MERGE);

            mParseTree.setNodeById(i, node);

            if (token.getDependencyEdge().getHeadTokenIndex() == i)
                mParseTree.changeRoot(i);
            else {
                node.setParentId(token.getDependencyEdge().getHeadTokenIndex());
                ParseTree.Node parent = mParseTree.getNodeById(node.getParentId());
                if (parent == null)
                    parent = new ParseTree.Node();
                parent.addChildrenId(i);
                mParseTree.setNodeById(node.getParentId(), parent);
            }
        }
    }

}