package hudson.plugins.cppncss.parser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Run;
import hudson.util.IOException2;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.annotation.CheckForNull;
import java.io.*;
import java.util.*;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @author Shaohua Wen
 * @since 25-Feb-2008 21:33:40
 */
@SuppressFBWarnings(value = "SE_NO_SERIALVERSIONID", justification = "Ignored in XStream")
public class Statistic implements Serializable {
// ------------------------------ FIELDS ------------------------------

    /**
     * @deprecated this field should not be used
     */
    @CheckForNull
    private transient Run<?, ?> owner;

    private String name;
    private long functions;
    private long ncss;
    private long ccn;
    private String parentElement;

// -------------------------- STATIC METHODS --------------------------

    public static StatisticsResult parse(File inFile) throws IOException, XmlPullParserException {
    	StatisticsResult result = new StatisticsResult();
        Collection<Statistic> fileResults = new ArrayList<Statistic>();
        Collection<Statistic> functionResults = new ArrayList<Statistic>();
        
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(inFile);
            bis = new BufferedInputStream(fis);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(bis, null);

            // check that the first tag is <cppncss>
            expectNextTag(parser, "cppncss");

            // skip until we get to the <measure> tag
            while (parser.getDepth() > 0 && (parser.getEventType() != XmlPullParser.START_TAG 
            		|| !"measure".equals(parser.getName()))) {
                parser.next();
            }

        	int depth = parser.getDepth();
        	//skip until we get to <item> tag
        	while(parser.getDepth() > 1 && (parser.getEventType() != XmlPullParser.START_TAG || !"item".equals(parser.getName()))) {
        		parser.next();
        	}
        	
        	while(parser.getDepth() >= depth) {
    			if(parser.getDepth() == 3 && parser.getEventType() == XmlPullParser.START_TAG && "item".equals(parser.getName())){
    				String functionName = parser.getAttributeValue(0);
    				Map<String, String> data = new HashMap<String, String>();
    				data.put("name", functionName);
            		String[] functionValueNames = {"Nr.","NCSS","CCN"};
            		int subIndex = 0;
            		String lastTag = null;
                    String lastText = null;
            		while (parser.getDepth() >= 3) {
            			parser.next();
            			switch (parser.getEventType()) {
            			 case XmlPullParser.START_TAG:
                             lastTag = parser.getName();
                             break;
            			 case XmlPullParser.TEXT:
                             lastText = parser.getText();
                             break;
            			 case XmlPullParser.END_TAG:
            				 subIndex ++;
                             if (parser.getDepth() == 4 && lastTag != null && lastText != null) {
                                 data.put(functionValueNames[subIndex - 1 ], lastText);
                             }
                             lastTag = null;
                             lastText = null;
                             break;
                         default:
                             // Do nothing
                             break;
            			}
                    }
            		Statistic s = new Statistic(data.get("name"));
            		if(data.get("name").indexOf(" at ") > 0){
            			String fileStr = data.get("name").substring(data.get("name").indexOf(" at ")+4);
            			String file = fileStr.substring(0,fileStr.lastIndexOf(":"));
            			s.setParentElement(file);
            		}
            		s.setNcss(Long.parseLong(data.get(functionValueNames[1]).trim()));
            		s.setCcn(Long.parseLong(data.get(functionValueNames[2]).trim()));
            		functionResults.add(s);
    			}
    			parser.next();
        	}
        	
        	 // skip until we get to the <measure> tag
            while (parser.getDepth() > 0 && (parser.getEventType() != XmlPullParser.START_TAG 
            		|| !"measure".equals(parser.getName()))) {
                parser.next();
            }
            
        	//skip until we get to <item> tag
        	while(parser.getDepth() > 1 && (parser.getEventType() != XmlPullParser.START_TAG || !"item".equals(parser.getName()))) {
        		parser.next();
        	}
        	depth = parser.getDepth();
        	while(parser.getDepth() >= depth) {
    			if(parser.getDepth() == 3 && parser.getEventType() == XmlPullParser.START_TAG && "item".equals(parser.getName())){
    				String fileName = parser.getAttributeValue(0);
    				Map<String, String> data = new HashMap<String, String>();
    				data.put("name", fileName);
            		String[] fileValueNames = {"Nr.","NCSS","CCN","Functions"};
            		int subIndex = 0;
            		String lastTag = null;
                    String lastText = null;
            		while (parser.getDepth() >= 3) {
            			parser.next();
            			switch (parser.getEventType()) {
            			 case XmlPullParser.START_TAG:
                             lastTag = parser.getName();
                             break;
            			 case XmlPullParser.TEXT:
                             lastText = parser.getText();
                             break;
            			 case XmlPullParser.END_TAG:
            				 subIndex ++;
                             if (parser.getDepth() == 4 && lastTag != null && lastText != null) {
                                 data.put(fileValueNames[subIndex - 1 ], lastText);
                             }
                             lastTag = null;
                             lastText = null;
                             break;
                         default:
                             // Do nothing
                             break;
            			}
                    }
            		Statistic s = new Statistic(data.get("name"));
            		s.setNcss(Long.parseLong(data.get(fileValueNames[1]).trim()));
            		s.setCcn(Long.parseLong(data.get(fileValueNames[2]).trim()));
            		s.setFunctions(Long.parseLong(data.get(fileValueNames[3]).trim()));
            		fileResults.add(s);
    			}
    			parser.next();
        	}

        } catch (XmlPullParserException e) {
            throw new IOException2(e);
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        
        result.setFunctionResults(functionResults);
        result.setFileResults(fileResults);
        return  result;
    }

    private static boolean skipToTag(XmlPullParser parser, String tagName)
            throws IOException, XmlPullParserException {
        while (true) {
            if (parser.getEventType() == XmlPullParser.END_TAG) {
                return false;
            }
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                parser.next();
                continue;
            }
            if (parser.getName().equals(tagName)) {
                return true;
            }
            skipTag(parser);
        }
    }

    private static void skipTag(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.next();
        endElement(parser);
    }

    private static void expectNextTag(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        while (true) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                parser.next();
                continue;
            }
            if (parser.getName().equals(tag)) {
                return;
            }
            throw new IOException("Expecting tag " + tag);
        }
    }

    private static void endElement(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        int depth = parser.getDepth();
        while (parser.getDepth() >= depth) {
            parser.next();
        }
    }

    public static Statistic total(Collection<Statistic>... results) {
        Collection<Statistic> merged = merge(results);
        Statistic total = new Statistic("");
        for (Statistic individual : merged) {
            total.add(individual);
        }
        return total;
    }

    public void add(Statistic r) {
        functions += r.functions;
        ncss += r.ncss;
        ccn += r.ccn;
    }

    public static Collection<Statistic> merge(Collection<Statistic>... results) {
        Collection<Statistic> newResults = new ArrayList<Statistic>();
        if (results.length == 0) {
            return Collections.emptySet();
        } else if (results.length == 1) {
            return results[0];
        } else {
            
            List<String> indivNames = new ArrayList<String>();
            for (Collection<Statistic> result : results) {
                for (Statistic individual : result) {
                    if (!indivNames.contains(individual.name)) {
                        indivNames.add(individual.name);
                    }
                }
            }

            for (String indivName : indivNames) {
                Statistic indivStat = new Statistic(indivName);
                for (Collection<Statistic> result : results) {
                    for (Statistic individual : result) {
                        if (indivName.equals(individual.name)) {
                            indivStat.add(individual);
                        }
                    }
                }
                newResults.add(indivStat);
            }
            return newResults;
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public Statistic(String name) {
        this.name = name;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCcn(long ccn) {
		this.ccn = ccn;
	}

	public long getCcn() {
		return ccn;
	}

	public long getFunctions() {
        return functions;
    }

    public void setFunctions(long functions) {
        this.functions = functions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNcss() {
        return ncss;
    }

    public void setNcss(long ncss) {
        this.ncss = ncss;
    }

    /**
     * @deprecated this field is not really supposed to be used.
     */
    @Deprecated
    @CheckForNull
	public Run<?, ?> getOwner() {
        return owner;
    }

    public void setOwner(Run<?, ?> owner) {
        this.owner = owner;
    }
    
    public void setParentElement(String parentElement) {
    	this.parentElement = parentElement;
    }
    
    public String getParentElement(){
    	return parentElement;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Statistic statistic = (Statistic) o;

        if (ccn != statistic.ccn) return false;
        if (functions != statistic.functions) return false;
        if (ncss != statistic.ncss) return false;
        if (!name.equals(statistic.name)) return false;
        if (owner != null ? !owner.equals(statistic.owner) : statistic.owner != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (owner != null ? owner.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

    public String toString() {
        return "Statistic{" +
                "name='" + name + '\'' +
                ", ccn=" + ccn +
                ", functions=" + functions +
                ", ncss=" + ncss +
                '}';
    }

    /**
     * @deprecated use {@link #getStatisticSummary()}
     */
    @Deprecated
    public String toSummary() {
        return getStatisticSummary().getHtmlSummary();
    }

    /**
     * @since TODO
     */
    public StatisticSummary getStatisticSummary() {
        return new FormattedStatisticSummary(ccn, functions, ncss);
    }

    /**
     * @deprecated Use {@link #getStatisticSummary(Statistic)}
     */
    @Deprecated
    public String toSummary(Statistic totals) {
        return getStatisticSummary(totals).getHtmlSummary();
    }

    /**
     * @since TODO
     */
    public StatisticSummary getStatisticSummary(Statistic totals) {
        return new FormattedStatisticSummary(totals, this);
    }

    public void set(Statistic that) {
        this.name = that.name;
        this.ccn = that.ccn;
        this.functions = that.functions;
        this.ncss = that.ncss;
    }
}
