/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package wap.quiz;

import org.apache.tapestry.ApplicationServlet;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.request.RequestContext;
import org.apache.tapestry.resource.ContextResourceLocation;
import org.apache.tapestry.util.StringSplitter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Global object for the Quiz application.
 *  This class has the following responsabilities:
 *  <ul>
 *  <li>Read questions from the specified files</li>
 *  <li>Provide questions by index</li>
 *  <li>Keep high scores in memory</li>
 *  </ul>
 *
 *  @version $Id$
 *  @author David Solis
 *
 **/

public class Global implements Serializable {
    private boolean init = false;

    private int points[];
    private String names[];

    private static final List easyQuestions = new ArrayList();
    private static final List mediumQuestions = new ArrayList();
    private static final List hardQuestions = new ArrayList();

    public static final int EASY_QUESTIONS = 1;
    public static final int MEDIUM_QUESTIONS = 2;
    public static final int HARD_QUESTIONS = 3;

    public static final String EASY_LEVEL = "easy";
    public static final String MEDIUM_LEVEL = "medium";
    public static final String HARD_LEVEL = "hard";

    public static final String QUESTION_KEY = "question";
    public static final String ANSWER_KEY = "answer";
    public static final String CHOICES_KEY = "choices";

    public List getHighscores()
    {
        List result = new ArrayList();
        for (int i = 0; i < 9; i++)
        {
            String name = names[i];
            if (!"nobody".equals(name))
            {
                Map map = new HashMap();
                map.put("name", name);
                map.put("score", new Integer(points[i]));
                result.add(map);
            }
        }
        return result;
    }

    /**
     *  Checks if score is high scores
     **/
    public boolean isHighscore(int score)
    {
        return (score >= points[9]);
    }

    /**
     *  Checks if score is a new high score
     **/
    public boolean isNewHighscore(int score)
    {
        return (score > points[9]);
    }

    /**
     *  Adds score/name to the highscore table,
     *  returns position in highscore table or -1 if no highscore
     **/
    public int addHighscore(int score, String name)
    {
        int i = -1;
        if (isNewHighscore(score)) {
            for (i = 0; i <= 9; i++) {
                if (score > points[i])
                    break;
            }
            for (int j = 9; j >= i + 1; j--) {
                points[j] = points[j - 1];
                names[j] = names[j - 1];
            }
            points[i] = score;
            names[i] = name;
        }
        return i;
    }

    /**
     * "getQuestionSet" method returns a number representing the right difficulty setting.
     *
     * @param level an <code>String</code> value
     * @return an <code>int</code> value
     */
    public int getQuestionSet(String level)
    {
        if (HARD_LEVEL.equals(level))
            return HARD_QUESTIONS;
        if (MEDIUM_LEVEL.equals(level))
            return MEDIUM_QUESTIONS;
        return EASY_QUESTIONS;
    }

    /**
     * "getQuestion" method returns a String that contains the question, the alternatives and the correct answer.
     *
     * @param questionToBeAsked an <code>int</code> value
     * @param questionSet an <code>int</code> value
     * @return a <code>String</code> value
     */
    public Map getQuestion(int questionToBeAsked, int questionSet)
    {
        String line;
        List list;

        switch (questionSet) {
            case EASY_QUESTIONS:
                list = easyQuestions;
                break;
            case MEDIUM_QUESTIONS:
                list = mediumQuestions;
                break;
            default:
                list = hardQuestions;
                break;
        }
        line = (String) list.get(questionToBeAsked);
        StringSplitter splitter = new StringSplitter(';');
        String[] result = splitter.splitToArray(line);
        HashMap map = new HashMap();
        map.put(QUESTION_KEY, result[0]);
        int answer = Integer.parseInt(result[1]) - 1;
        map.put(ANSWER_KEY, new Integer(answer));
        int length = result.length - 2;
        String[] choices = new String[length];
        System.arraycopy(result, 2, choices, 0, length);
        map.put(CHOICES_KEY, choices);
        return map;
    }

    /**
     * "getNumberOfQuestions" method returns the number of questions (Strings) in the questions table that is used.
     *
     * @param questionSet an <code>int</code> value
     * @return an <code>int</code> value
     */
    public int getNumberOfQuestions(int questionSet) {
        switch (questionSet) {
            case EASY_QUESTIONS:
                return easyQuestions.size();
            case MEDIUM_QUESTIONS:
                return mediumQuestions.size();
            default:
                return hardQuestions.size();
        }
    }

    public void initialize(IRequestCycle cycle) {
        if (!init) {
            RequestContext requestContext = cycle.getRequestContext();
            ApplicationServlet servlet = requestContext.getServlet();
            ServletConfig config = servlet.getServletConfig();
            ServletContext context = servlet.getServletContext();
            IResourceLocation webInfLocation = new ContextResourceLocation(context, "/WEB-INF/");
            IResourceLocation webInfAppLocation = webInfLocation.getRelativeLocation(servlet.getServletName() + "/");
            readQuestions(easyQuestions, webInfAppLocation, config.getInitParameter("easyquestionsfile"));
            readQuestions(mediumQuestions, webInfAppLocation, config.getInitParameter("mediumquestiosfile"));
            readQuestions(hardQuestions, webInfAppLocation, config.getInitParameter("hardquestionsfile"));
            points = new int[10];
            names = new String[10];
            for (int i = 0; i <= 9; i++) {
                points[i] = 0;
                names[i] = "nobody";
            }
            init = true;
        }
    }

    /**
     * This method reads the questions from the appropriate file to the appropriate Vector.
     *
     * @param questions a <code>Vector</code> value
     * @param questionsFile a <code>String</code> value
     * @exception java.io.IOException if an error occurs
     */
    private void readQuestions(List questions, String questionsFile) throws IOException
    {
        BufferedReader reader = null;
        String line;
        reader = new BufferedReader(new FileReader(questionsFile));
        while ((line = reader.readLine()) != null) {
            if (line.trim().equals("")) break;
            questions.add(line);
        }
    }

    private void readQuestions(List questions, IResourceLocation location, String filename)
    {
        IResourceLocation result = location.getRelativeLocation(filename);
        URL url = result.getResourceURL();
        try {
            readQuestions(questions, url.getFile());
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

}
