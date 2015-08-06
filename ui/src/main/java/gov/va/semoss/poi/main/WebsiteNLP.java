package gov.va.semoss.poi.main;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringReader;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.va.semoss.util.DIHelper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import org.apache.log4j.Logger;
import gov.va.semoss.util.Constants;
public class WebsiteNLP {
	private static final Logger log = Logger.getLogger( WebsiteNLP.class );
  
	//class variables
	static List<TypedDependency> tdl = new ArrayList();
	static List<TypedDependency> tdl2 = new ArrayList();
	static ArrayList <TaggedWord> TaggedWords = new ArrayList();
	static ArrayList <CoreLabel> CoreLabels = new ArrayList();
	static ArrayList <WordLemmaTag> WordLemmas = new ArrayList();
	static ArrayList <TripleWrapper> Triples = new ArrayList();
	static Hashtable <GrammaticalRelation, Vector<TypedDependency>> nodeHash = new Hashtable<GrammaticalRelation, Vector<TypedDependency>>();
	static Vector <TypedDependency> dobjV = new Vector<TypedDependency>();
  static Vector <TypedDependency> subjV = new Vector<TypedDependency>();
  static Hashtable <String, String> negHash = new Hashtable<String, String>();
  static Hashtable<String, Vector<String>> VNhash = new Hashtable<String,Vector<String>>();
  Hashtable<String, Vector<String>> VNFNhash = new Hashtable<String,Vector<String>>();
  static List<RelationSheet> excelfiller = new ArrayList();
  static boolean SentenceParsable = true;
  static LexicalizedParser lp 
      = LexicalizedParser.loadModel(DIHelper.getInstance().getProperty(Constants.BASE_FOLDER)
      +File.separator+"NLPartifacts"+File.separator+"englishPCFG.ser"); //<--TODO path to grammar goes here
  StanfordCoreNLP pipeline = new StanfordCoreNLP();
  static XSSFWorkbook wb = new XSSFWorkbook();
	XSSFSheet sheetToWriteOver;
	XSSFRow rowToWriteOn;
	XSSFCell cellToWriteOn;
	static int ArticleNUM = 0;
    
	public static ArrayList<TripleWrapper> MasterRead(String[] files) throws Exception{
		// TODO Auto-generated method stub
				WebsiteNLP test = new WebsiteNLP();
				Triples = new ArrayList();
				for(ArticleNUM = 0; ArticleNUM<files.length; ArticleNUM++){	
						wb = new XSSFWorkbook();
						excelfiller = new ArrayList();
						String docin = files[ArticleNUM];
						String docout = "Future path for output files related to the doc"; //excel sheet that is made
						log.debug("MMC "+docin);
						NLP(docin,docout);
						
					}
				log.debug("MMC DONE PROCESSING FILES");
					test.TrimTriples();
					test.createOccuranceCount();
					test.SetupSheet();
					test.Fillexcel(DIHelper.getInstance().getProperty(Constants.BASE_FOLDER)
				              +File.separator+"NLPartifacts"+File.separator+"AllArticles.xlsx");
					log.debug("BIG DONE");
					return Triples;
	}
	

	private void createOccuranceCount() {
		log.debug("COUNT TABLE PRINTED ");
		// TODO Auto-generated method stub
		
		// Hashtable <String, Integer> OccurCount = new Hashtable<String, Integer>();
		 ArrayList <String> term = new ArrayList();
		 ArrayList <Integer> termcount = new ArrayList();
		 int count=0;
		 int indexofcount = 0;
		for(int i = 0; i<Triples.size(); i++){
	
			if(term.contains(Triples.get(i).getObj1())){
				indexofcount = term.indexOf(Triples.get(i).getObj1());
				termcount.set(indexofcount, termcount.get(indexofcount)+1);}
			else{
				term.add(Triples.get(i).getObj1());
				termcount.add(1);}
			if(term.contains(Triples.get(i).getPred())){
				indexofcount = term.indexOf(Triples.get(i).getPred());
				termcount.set(indexofcount, termcount.get(indexofcount)+1);}
			else{
				term.add(Triples.get(i).getPred());
				termcount.add(1);}
			if(term.contains(Triples.get(i).getObj2())){
				indexofcount = term.indexOf(Triples.get(i).getObj2());
				termcount.set(indexofcount, termcount.get(indexofcount)+1);}
			else{
				term.add(Triples.get(i).getObj2());
				termcount.add(1);}
			
			
		}
		log.debug("COUNT TABLE PRINTED "+ term);
		log.debug("COUNT TABLE PRINTED "+ termcount);
		
		for(int i = 0; i<Triples.size(); i++){
			if(term.contains(Triples.get(i).getObj1())){
				indexofcount = term.indexOf(Triples.get(i).getObj1());
				Triples.get(i).setObj1num(termcount.get(indexofcount));   
			}
			if(term.contains(Triples.get(i).getPred())){
				indexofcount = term.indexOf(Triples.get(i).getPred());
				Triples.get(i).setPrednum(termcount.get(indexofcount));   
			}
			if(term.contains(Triples.get(i).getObj2())){
				indexofcount = term.indexOf(Triples.get(i).getObj2());
				Triples.get(i).setObj2num(termcount.get(indexofcount));   
			}
		}
		log.debug("TriplesNum");
		for(int i = 0; i <Triples.size();i++){
			log.debug(Triples.get(i).getObj1num());
			log.debug(Triples.get(i).getPrednum());
			log.debug(Triples.get(i).getObj2num());
		}
		
		
		
	}


	private static void NLP(String docin, String docout) throws Exception{
		
		int TripleIndex = 0;
		List<String> DocSentences = new ArrayList<String>();
		WebsiteNLP test = new WebsiteNLP();
		//test.createVNFN();
		
		test.ReadDoc(DocSentences, docin);
	//	test.WriteDoc(DocSentences, docout);
		
		log.debug("MMC " + DocSentences);
		lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
		for(int i = 0; i<DocSentences.size(); i++) //DocSentences.size()
		{
			tdl = new ArrayList();
			tdl2 = new ArrayList();;
			TaggedWords = new ArrayList();
			CoreLabels = new ArrayList();
			WordLemmas = new ArrayList();
			nodeHash = new Hashtable<GrammaticalRelation, Vector<TypedDependency>>();
			dobjV = new Vector<TypedDependency>();
		    subjV = new Vector<TypedDependency>();
		    negHash = new Hashtable<String, String>();
		   
		
		tdl = test.CreateDepList(DocSentences.get(i), tdl, TaggedWords); //create dependencies
		if(SentenceParsable == true)
			{
		//	test.GetLemma(DocSentences.get(i));//is the example code
		//	test.SetLemma(tdl,WordLemmas);
		//	log.debug(WordLemmas);
		//	log.debug(tdl);
			test.SetHash(tdl, nodeHash);
		//	log.debug();
			//log.debug(nodeHash);
			TripleIndex = Triples.size();
			test.GetTriples();
		//	test.setLemmaTriples(TripleIndex);
			}
		}
		log.debug("Result");
	//	test.TrimTriples();
		//test.SwitchToPred();
		log.debug(Triples);
		log.debug("STARTING FINAL STAGE");
	//	test.SetupSheet();
	//	test.Fillexcel(docout);
	//	log.debug("BIG DONE");
	}
	private void setLemmaTriples(int tripleIndex) {
		int index = 0;
		for(int i = tripleIndex; i<Triples.size(); i++){
			index = Integer.parseInt(Triples.get(i).getPred().toString().substring(Triples.get(i).getPred().toString().length()-1, Triples.get(i).getPred().toString().length()));
			index = index-1;
			Triples.get(i).setNormPred(WordLemmas.get(index).toString().substring(WordLemmas.get(index).toString().indexOf('/')+1,WordLemmas.get(index).toString().lastIndexOf('/')));
		//	log.debug("TEST");
			//log.debug(index+" ah "+TaggedWords.get(index)+" ph "+WordLemmas.get(index)+" dh "+Triples.get(i).getPred().toString());
		//	log.debug(Triples.get(i).getPred()+" ah "+Triples.get(i).getNormPred());
		//	log.debug("TEST2");
			
			}
		
		
		
		
	}
	private void WriteDoc(List<String> docSentences, String docout) throws IOException {
		// TODO Auto-generated method stub
		 FileWriter fstream = new FileWriter(docout);
	        BufferedWriter out = new BufferedWriter(fstream);
	       // out.write("TEST");
	   for(int i = 0; i<docSentences.size(); i++){
	       out.write(docSentences.get(i));
	        out.newLine();
	   }
	 out.close();
		
	}
	private void SwitchToPred(){
		String temp = "Failed";
		for(int i = 0; i<Triples.size();i++){
			log.debug(Triples.get(i).getPred().toString());
			if(Triples.get(i).getPred().toString().equals("to")){
				log.debug("HHHHHHHHERE");
				temp = Triples.get(i).getPred().toString();
				Triples.get(i).setPred(Triples.get(i).getPredexp().toString());
				Triples.get(i).setPredexp(temp);
			}
		}
	}
	private void TrimTriples() {
		for(int i = 0; i<Triples.size(); i++)
		{
		log.debug("HERE");
		log.debug(Triples.get(i).getObj1());
		log.debug("HERE");
		Triples.get(i).setObj1(Triples.get(i).getObj1().toString().substring(0, Triples.get(i).getObj1().toString().indexOf('-')));
		Triples.get(i).setPred(Triples.get(i).getPred().toString().substring(0, Triples.get(i).getPred().toString().indexOf('-')));
		Triples.get(i).setObj2(Triples.get(i).getObj2().toString().substring(0, Triples.get(i).getObj2().toString().indexOf('-')));
		
		//set the expanded to NA if given no value
		if(Triples.get(i).getObj1exp().equals(""))
			Triples.get(i).setObj1exp("NA");
		if(Triples.get(i).getPredexp().equals(""))
			Triples.get(i).setPredexp("NA");
		if(Triples.get(i).getObj2exp().equals(""))
			Triples.get(i).setObj2exp("NA");
		
		
		
		Triples.get(i).setObj1exp(Triples.get(i).getObj1exp().toString().replace("'", ","));
		Triples.get(i).setObj1exp(Triples.get(i).getObj1exp().toString().replace("`", ","));
		Triples.get(i).setPredexp(Triples.get(i).getPredexp().toString().replace("'", ","));
		Triples.get(i).setPredexp(Triples.get(i).getPredexp().toString().replace("`", ","));
		Triples.get(i).setObj2exp(Triples.get(i).getObj2exp().toString().replace("'", ","));
		Triples.get(i).setObj2exp(Triples.get(i).getObj2exp().toString().replace("`", ","));
		
		
		
		}
		
	}
	public void ReadDoc(List<String> DocSentences2, String docin) throws Exception{
		//need to deal with return carriage!!!
		//logic for website, .docx .doc branch resume
		if(docin.contains("http")){
			//source is website
			Scanner scan;
			TextExtractor textExtractor = new TextExtractor();
			String extractedText = textExtractor.WebsiteTextExtractor(docin);
			scan = new Scanner(extractedText);
			log.debug("Processing Website");
			int j = 0;
			scan.useDelimiter("\\. *\\s|\\? *\\s|\\! *\\s");
			while (scan.hasNext()){
				DocSentences2.add(scan.next()+".");
				DocSentences2.get(j).replaceAll("\\r\\n|\\r|\\n", " ").replace("\n","").replace("\r", "");
				log.debug(DocSentences2.get(j));
				j++;
			}
			log.debug("done");
			scan.close();
		}
		if(docin.contains(".doc")){
			//source is a wordocument
			Scanner scan;
			TextExtractor textExtractor = new TextExtractor();
			String extractedText = textExtractor.WorddocTextExtractor(docin);
			String ResumeName = "NotResumeDoc";
			log.debug("Processing generic document");
			if(extractedText.contains("Deloitte Consulting LLP")){
				ResumeName = extractedText.substring(0, extractedText.indexOf("Deloitte Consulting LLP"));
				log.debug("ResumeName "+ResumeName);
				ResumeName = ResumeName.substring(0, ResumeName.lastIndexOf("@")-4);
				log.debug("ResumeName "+ResumeName);
				ResumeName = ResumeName.substring(0, ResumeName.lastIndexOf("@")-4);
				log.debug("ResumeName "+ResumeName);
				ResumeName = ResumeName.substring(ResumeName.lastIndexOf("@")+1,ResumeName.length()).trim();
				log.debug("ResumeName "+ResumeName);
				ResumeName = ResumeName.substring(0, ResumeName.indexOf(" "));
				}
			scan = new Scanner(extractedText);
			log.debug("Processing resume document");
			int j = 0;
			scan.useDelimiter("\\. *\\s|\\? *\\s|\\! *\\s");
			while (scan.hasNext()){
				DocSentences2.add(scan.next()+".");
				DocSentences2.get(j).replaceAll("\\r\\n|\\r|\\n", " ").replace("\n","").replace("\r", "");
				log.debug(DocSentences2.get(j));
				j++;
			}
			if(extractedText.contains("Deloitte Consulting LLP")){
			ResumeProcessing(DocSentences2, ResumeName);
			}
		}
		if(docin.contains(".txt"))
		{
			//source is a .txt file
		}
		
	}
	public void ResumeProcessing(List<String> DocSentences3, String resumeName){
		Scanner scan;
		for(int i =0; i<DocSentences3.size(); i++)
		{
			if(DocSentences3.get(i).contains("Role:"))
			{
				DocSentences3.set(i, DocSentences3.get(i).replace("Role:", ""));
				log.debug("FOUND ROLE");
				scan = new Scanner(DocSentences3.get(i));
				scan.useDelimiter("\\@ ");
				while (scan.hasNext()){
    				DocSentences3.add(resumeName +" "+scan.next()+". ");
				}
				log.debug("DELETED HOPE "+DocSentences3.get(i));
				DocSentences3.remove(i);
				
			}
			else{
				DocSentences3.set(i, DocSentences3.get(i).replace("@", ""));
			}
		}
		log.debug("PostResumeProcessing Sentences");
		
		for(int i = 0; i<DocSentences3.size(); i++)
			log.debug(DocSentences3.get(i));
	}
	public void createVNFN() throws FileNotFoundException
	{
		List<String> verbNet = new ArrayList<String>();
		List<String> frameNet = new ArrayList<String>();
		List<String> lemma = new ArrayList<String>();
		List<String> frame = new ArrayList<String>();
		//Hashtable<String, Vector<String>> hash = new Hashtable<String,Vector<String>>();
		String[] names = null;
		// read in text file
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(new File(DIHelper.getInstance().getProperty(Constants.BASE_FOLDER)+"//NLPartifacts//VNC-FNF.txt"));
		while (scan.hasNextLine()){
			verbNet.add(scan.nextLine());
		}
		// keep only the lines that contain the relationship between VerbNet and FrameNet
		// create a new ArrayList
		for (int i = 0; i < verbNet.size(); i++) {
			String string = verbNet.get(i);
			if(string.contains("vncls class=")) {
				frameNet.add(string);
			}
		}
		
		// put the lemmas and frames into separate array lists
		for (int i = 0; i < frameNet.size(); i++) {
			Vector <String> baseVector = new Vector<String>();;
			String Lemma = null;
			String Frame = null;
			String string = frameNet.get(i);
			names = string.split("'");
			lemma.add(names[3]);
			frame.add(names[5]);
			Lemma = names[3];
			Frame = names[5];
			if(VNhash.containsKey(Lemma)){
					baseVector = VNhash.get(Lemma);
				//	log.debug("HERE");
				}
				baseVector.addElement(Frame);
				VNhash.put(Lemma, baseVector);
		}
		for(int i = 0; i<lemma.size();i++){
	//	log.debug(lemma.get(i)+" "+frame.get(i));
		}
	//	log.debug(VNhash);
	}
	public void setVNFNinTriples()
	{
			for(int i = 0; i<Triples.size(); i++){
				//?? how to map, question?
			}
	}
	private void SetLemma(List<TypedDependency> tdl1, ArrayList<WordLemmaTag> wordLemmas2) {
			for(int i = 0; i<tdl1.size(); i++){
				for(int j = 0; j<wordLemmas2.size(); j++)
				{
					if(tdl1.get(i).dep().toString().equals(wordLemmas2.get(j).value()))
					{
						tdl1.get(i).dep().setValue(wordLemmas2.get(j).lemma());
					}
					if(tdl1.get(i).gov().toString().equals(wordLemmas2.get(j).value()))
					{
						tdl1.get(i).gov().setValue(wordLemmas2.get(j).lemma());
					}
					
				}
			}
		//	log.debug("TEST TEST");
		//	log.debug(tdl1);
	}
	public List<String> ReadTextDoc(String textdoc){
		List<String> Sentences = null;
		return Sentences;
	}
	public List<TypedDependency> CreateDepList(String TheSentence, List<TypedDependency> tdl, List<TaggedWord> TaggedWords)
	{
			//picking the grammer sheet to use for parsing
		 
		    TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		    ////This structures the sentence - needs sentence as an input and would return a list of typedependencies
			List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(TheSentence)).tokenize();
			Tree bestParse = lp.parseTree(rawWords);
		    
		    Tree parse = bestParse; 
		//    log.debug("Sentence Tree");
		//    log.debug(bestParse); //prints the sentence with () and POS
		   // bestParse.pennPrint(); //prints sentence in heirarchical fashion
		    try{
		        TaggedWords.addAll(bestParse.taggedYield()); //gives each word with its POS
		        SentenceParsable = true;
		    }
		    catch(NullPointerException e ){
		        log.debug("This Sentence failed: "+ TheSentence);
		        SentenceParsable = false;
		        return tdl;
		        }
		    CoreLabels.addAll( bestParse.taggedLabeledYield());
		//    log.debug("POS");
		    log.debug("From createDep: "+ TheSentence);
		    log.debug("From createDep: "+ TaggedWords);
		   // log.debug(CoreLabels.get(1).setLemma(lemma););
		    
		    GrammaticalStructure gs = null;
		    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		    gs = gsf.newGrammaticalStructure(parse);
		     
		    tdl = gs.typedDependenciesCCprocessed(); //@@choose which set of dependencies you want
		 //   log.debug("dependencies");
		  //  log.debug(tdl);
		 ////done structuring the sentence
		    
		return tdl;
	}
	public void FindRelation(){
		
	}
	public List<TypedDependency> AddConjDep(List<TypedDependency> tdl){
		//logic to determine which TypedDependencies to add
		//TypedDependency e = new TypedDependency();//you need (grammaticalrelation, Treegraphnode, Treegraphnode)
		//Then add the new Typed Dependency
		return tdl;
	}
	public void GetLemma(String TheSentence){
		int i = 0;
		WordLemmaTag temp = new WordLemmaTag();
		 Annotation annotation = new Annotation(TheSentence);
		 pipeline.annotate(annotation);
		 //log.debug("HERE");
		 List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		 //log.debug( sentences.get(0).get(CoreAnnotations.LemmaAnnotation.class).toString());
		  
		    if (sentences != null && sentences.size() > 0) {
		      ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(0);
		     // log.debug("The first sentence is:");
		     // log.debug(sentence.toShorterString());
		      Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
		    //  log.debug();
		    //  log.debug("The first sentence tokens are:");
		      for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
		    	  temp = new WordLemmaTag();
		    	  ArrayCoreMap aToken = (ArrayCoreMap) token;
		       //   log.debug(aToken.toShorterString());
		          temp.setWord(aToken.get(CoreAnnotations.ValueAnnotation.class).toString() + "-" + aToken.get(CoreAnnotations.TokenEndAnnotation.class).toString());
		          temp.setLemma(aToken.get(CoreAnnotations.LemmaAnnotation.class).toString());
		          WordLemmas.add(temp);
		      //    log.debug(WordLemmas.get(i).lemma());
		          i++;
		      }
		    }
	}
	
	public Hashtable <GrammaticalRelation, Vector<TypedDependency>> SetHash(List<TypedDependency> tdl,Hashtable <GrammaticalRelation, Vector<TypedDependency>> nodeHashA)
	{
		for(int tdlIndex = 0;tdlIndex < tdl.size();tdlIndex++)
	    {
		    TypedDependency one = tdl.get(tdlIndex);
		    String name = one.reln().getShortName();//get relation name in string
		    Vector <TypedDependency> baseVector = new Vector<TypedDependency>();
		    GrammaticalRelation rel = one.reln();
		    
		    if(nodeHashA.containsKey(rel)) //if this type of relation already exists
		    	baseVector = nodeHashA.get(rel);
		    baseVector.addElement(one);
		    nodeHashA.put(rel, baseVector);
	    }
		return nodeHashA;
	}
	 public void identifySubjandDobj(List<TypedDependency> tdl)
	 {
		    for(int tdlIndex = 0;tdlIndex < tdl.size();tdlIndex++)
		    {
			    TypedDependency one = tdl.get(tdlIndex);
			    // need to convert this to a hashtable of vectors
			    String relationName = one.reln().getShortName();
			    Vector <TypedDependency> baseVector = new Vector<TypedDependency>();
			    
			    String name = one.reln().getShortName();
			    //log.debug(" Relation is " + one.reln().getShortName() + EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
			    GrammaticalRelation rel = one.reln();
			    if(name.equalsIgnoreCase(EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER + ""))
			    		rel = EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER;
			    if(nodeHash.containsKey(rel))
			    	baseVector = nodeHash.get(rel);
			    baseVector.addElement(one);
			    nodeHash.put(rel, baseVector);
			   }
	 }
	 public void findVerbTriples(GrammaticalRelation subjR, GrammaticalRelation objR)
	 {
		 // based on the subjects and objects now find the predicates
		 dobjV = nodeHash.get(objR);
		 subjV = nodeHash.get(subjR);

		 if(dobjV != null && subjV != null)
		 {
			 for(int dobjIndex = 0;dobjIndex < dobjV.size();dobjIndex++)
			 {
				 TreeGraphNode obj = dobjV.get(dobjIndex).dep();
				 TreeGraphNode pred = dobjV.get(dobjIndex).gov();
				 String predicate = pred.value();
				 String object = obj + "";
				 //possibly add a clausal search for this as well
				 //log.debug("OBJ " + pred + "<<>>" + predicate + "<<>>" + obj);
				 			 
				 // now find the subject
				 //log.debug("Node in relation " + GrammaticalStructure.getNodeInRelation(obj, EnglishGrammaticalRelations.PREDICATE));
				 for(int subjIndex = 0;subjIndex < subjV.size();subjIndex++)
				 {
					 TreeGraphNode subj = subjV.get(subjIndex).dep();
					 TreeGraphNode dep2 = subjV.get(subjIndex).gov();
					 //log.debug("SUBJ" + subj + "<<>>" + subjV.get(subjIndex).dep());
					 if((dep2+"").equalsIgnoreCase(pred + "")) //Sam - !!!this is the comparison to determine if their is a chain
					 {
						 //CORE TRIPLES FOUND
						 TripleWrapper temp = new TripleWrapper();
						 //log.debug(">>>> " +obj.label().tag() + subj); 			 
						 String finalSubject  = subj.value();
						 String finalObject = "";
						 log.debug("VERB Triple simple: "+subj+" "+pred+" "+obj);
						 //Setting TripleWrapper
						 temp.setObj1(subj.toString()); //part of future SetTriple
						 temp.setPred(pred.toString());
						 temp.setObj2(obj.toString());
						 temp.setObj1POS(temp.getObj1(),TaggedWords);
						 temp.setPredPOS(temp.getPred(),TaggedWords);
						 temp.setObj2POS(temp.getObj2(),TaggedWords);
					//	 log.debug("TEST22");
					//	 log.debug(temp.getObj1POS());
					
						 
						 
						 //FINDING EXTENSION OF SUBJECT****
						 // find if complemented
						 // need to do this only if the subj is not a noun
						 // final subject
						 TreeGraphNode altPredicate = findCompObject(dep2);
						 if(!subj.label().tag().contains("NN") && ( nodeHash.containsKey(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT) || nodeHash.containsKey(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT)))
						 {		//log.debug("Came here");
							 subj = findComplementNoun(subj, dep2, EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
							 if(!subj.label().tag().contains("NN")){
								 //log.debug("XCalusal now");
								 subj = findCompSubject(dep2);
							 }
						 }		
						 finalObject = getFullNoun(obj);
						 finalObject = finalObject + findPrepNounForPredicate(pred);
						 finalSubject = getFullNoun(subj);
						 
						 //FINDING EXTENSION OF PREDICATE****
						 // find the negators for the predicates next
						 if(negHash.containsKey(pred + "")|| negHash.containsKey(altPredicate + "")){
							 predicate = "NOT " + predicate;
						 }
						 
						 //EXTENSION OF OBJECT FOUND****
						 // fulcrum on the nsubj to see if there is an NNP in the vicinity
						 if(finalObject.indexOf(predicate) < 0 && predicate.indexOf(finalObject) < 0){
							 //log.debug("verb triple");
							 log.debug("VERB Triple: 	" + finalSubject + "<<>>" + predicate + "<<>>" + finalObject);
						 }
						 temp.setObj1exp(finalSubject.toString());// part of future SetTriple
						 temp.setPredexp(predicate.toString());
						 temp.setObj2exp(finalObject.toString());
						 temp.setArticleNum(Integer.toString(ArticleNUM));
						 Triples.add(temp);
						 temp = new TripleWrapper();//be sure this line happens before you add another triple or they will point to the same thing
						 //subjV.remove(subjIndex);
					 }
				 }
			 }
			 //nodeHash.put(subjR, subjV);
			 //nodeHash.put(objR, dobjV);
		 }
	 }
	 public void findPrepTriples(GrammaticalRelation subjR, GrammaticalRelation objR, GrammaticalRelation Pobj)
	 {
		 // based on the subjects and objects now find the predicates
		 dobjV = nodeHash.get(objR);
		 subjV = nodeHash.get(subjR);

		 if(dobjV != null && subjV != null)
		 {
			 for(int dobjIndex = 0;dobjIndex < dobjV.size();dobjIndex++)
			 {
				 TreeGraphNode obj = dobjV.get(dobjIndex).dep();
				 TreeGraphNode pred = dobjV.get(dobjIndex).gov();
				 String predicate = pred.value();
				 String object = obj + "";
				 //possibly add a clausal search for this as well
				 //log.debug("OBJ " + pred + "<<>>" + predicate + "<<>>" + obj);
				 			 
				 // now find the subject
				 //log.debug("Node in relation " + GrammaticalStructure.getNodeInRelation(obj, EnglishGrammaticalRelations.PREDICATE));
				 for(int subjIndex = 0;subjIndex < subjV.size();subjIndex++)
				 {
					 TreeGraphNode subj = subjV.get(subjIndex).dep();
					 TreeGraphNode dep2 = subjV.get(subjIndex).gov();
					 //log.debug("SUBJ" + subj + "<<>>" + subjV.get(subjIndex).dep());
					 if((dep2+"").equalsIgnoreCase(pred + "")) //Sam - !!!this is the comparison to determine if their is a chain
					 {
						 
						 //CORE TRIPLES FOUND
						 TripleWrapper temp = new TripleWrapper();
						 //log.debug(">>>> " +obj.label().tag() + subj); 			 
						 String finalSubject  = subj.value();
						 String finalObject = "";
						 log.debug("VERB Triple simple: "+subj+" "+pred+" "+obj);
						 //Setting TripleWrapper
						 temp.setObj1(subj.toString()); //part of future SetTriple
						 temp.setPred(pred.toString());
						 temp.setObj2(obj.toString());
						 temp.setObj1POS(temp.getObj1(),TaggedWords);
						 temp.setPredPOS(temp.getPred(),TaggedWords);
						 temp.setObj2POS(temp.getObj2(),TaggedWords);
					//	 log.debug("TEST22");
					//	 log.debug(temp.getObj1POS());
					
						 
						 
						 //FINDING EXTENSION OF SUBJECT****
						 // find if complemented
						 // need to do this only if the subj is not a noun
						 // final subject
						 TreeGraphNode altPredicate = findCompObject(dep2);
						 if(!subj.label().tag().contains("NN") && ( nodeHash.containsKey(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT) || nodeHash.containsKey(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT)))
						 {		//log.debug("Came here");
							 subj = findComplementNoun(subj, dep2, EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
							 if(!subj.label().tag().contains("NN")){
								 //log.debug("XCalusal now");
								 subj = findCompSubject(dep2);
							 }
						 }		
						 finalObject = getFullNoun(obj);
						 finalObject = finalObject + findPrepNounForPredicate(pred);
						 finalSubject = getFullNoun(subj);
						 
						 //FINDING EXTENSION OF PREDICATE****
						 // find the negators for the predicates next
						 if(negHash.containsKey(pred + "")|| negHash.containsKey(altPredicate + "")){
							 predicate = "NOT " + predicate;
						 }
						 
						 //EXTENSION OF OBJECT FOUND****
						 // fulcrum on the nsubj to see if there is an NNP in the vicinity
						 if(finalObject.indexOf(predicate) < 0 && predicate.indexOf(finalObject) < 0){
							 //log.debug("verb triple");
							 log.debug("VERB Triple: 	" + finalSubject + "<<>>" + predicate + "<<>>" + finalObject);
						 }
						 temp.setObj1exp(finalSubject.toString());// part of future SetTriple
						 temp.setPredexp(predicate.toString());
						 temp.setObj2exp(finalObject.toString());
						 Triples.add(temp);
						 temp = new TripleWrapper();//be sure this line happens before you add another triple or they will point to the same thing
						 //subjV.remove(subjIndex);
					 }
				 }
			 }
			 //nodeHash.put(subjR, subjV);
			 //nodeHash.put(objR, dobjV);
		 }
	 }
	 public void findBaseSubjectTriples(GrammaticalRelation subjR)
	 {
		 subjV = nodeHash.get(subjR);

		 if(subjV != null)
		 {
				 for(int subjIndex = 0;subjIndex < subjV.size();subjIndex++)
				 {
					 TreeGraphNode subj = subjV.get(subjIndex).dep();
					 TreeGraphNode dep2 = subjV.get(subjIndex).gov();
					 //log.debug("SUBJ" + subj + "<<>>" + subjV.get(subjIndex).dep());
					 String finalSubject  = subj.value();
					 String finalObject = "";

					 // find if complemented
					 // need to do this only if the subj is not a noun
					 // final subject
					 TreeGraphNode altPredicate = findCompObject(dep2);
					 
					 if(!subj.label().tag().contains("NN") && ( nodeHash.containsKey(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT) || nodeHash.containsKey(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT)))
					 {
						 //log.debug("Came here");
						 subj = findComplementNoun(subj, dep2, EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
						 if(!subj.label().tag().contains("NN"))
						 {
								 //log.debug("XCalusal now");
								 subj = findCompSubject(dep2);
							 }
						 }		
						 						 
					 //finalObject = getFullNoun(dep2);
					 finalObject = findPrepNounForPredicate(dep2);
					 finalSubject = getFullNoun(subj);
					 String pred = dep2.value();
					//--SAM triple found here
				//	 log.debug("BASE Triple "+finalSubject+" "+pred+" "+finalObject); 
						 // find the negators for the predicates next
						 if(negHash.containsKey(pred + "")|| negHash.containsKey(altPredicate + ""))
							 pred = "NOT " + pred;
						
						 // lemmatize em :)
						 
						 // find the relation for auxilliary
						 
						 // find the preparators next
						 
						 // fulcrum on the nsubj to see if there is an NNP in the vicinity
						 
						 if(finalObject.indexOf(pred) < 0 && pred.indexOf(finalObject) < 0){
							 log.debug("base subject predicate");
							 log.debug("BASE Triple" + finalSubject + "<<>>" + pred + "<<>>" + finalObject);
						 }
							 
						 
						 //subjV.remove(subjIndex);
					 }
				 }
			 //nodeHash.put(subjR, subjV);
			 //nodeHash.put(objR, dobjV);
	 }
	 public void findModTriples(GrammaticalRelation subjR, GrammaticalRelation objR)
	 {
		 // based on the subjects and objects now find the predicates
		 dobjV = nodeHash.get(objR);
		 subjV = nodeHash.get(subjR);

		 if(dobjV != null && subjV != null)
		 {
			 for(int dobjIndex = 0;dobjIndex < dobjV.size();dobjIndex++)
			 {
				 TreeGraphNode obj = dobjV.get(dobjIndex).gov();
				 TreeGraphNode pred = dobjV.get(dobjIndex).dep();
				 String predicate = pred.value();
				 String object = obj + "";
				 //log.debug("OBJ " + pred + "<<>>" + predicate + "<<>>" + obj);
				 			 
				 // now find the subject
				 //log.debug("Node in relation " + GrammaticalStructure.getNodeInRelation(obj, EnglishGrammaticalRelations.PREDICATE));
				 for(int subjIndex = 0;subjIndex < subjV.size();subjIndex++)
				 {
					 TreeGraphNode subj = subjV.get(subjIndex).dep();
					 TreeGraphNode dep2 = subjV.get(subjIndex).gov();
					 //log.debug("SUBJ" + subj + "<<>>" + subjV.get(subjIndex).dep());
					 if((dep2+"").equalsIgnoreCase(obj + ""))
					 {
						 TripleWrapper temp = new TripleWrapper();
						 //log.debug(">>>> " +obj.label().tag() + subj);
						 // need to navigate to the object if this is 			 
						 String finalSubject  = subj.value();
						 String finalObject = "";
						 //--SAM triple found here
						 log.debug("MOD Triple Simple : "+subj+"<<>>"+pred+"<<>>"+obj);
						 temp.setObj1(subj.toString()); //part of future SetTriple
						 temp.setPred(pred.toString());
						 temp.setObj2(obj.toString());
						 temp.setObj1POS(temp.getObj1(),TaggedWords);
						 temp.setPredPOS(temp.getPred(),TaggedWords);
						 temp.setObj2POS(temp.getObj2(),TaggedWords);
					//	 log.debug("TEST22");
					//	 log.debug(temp.getObj1POS());
						 // find if complemented
						 // need to do this only if the subj is not a noun
						 // final subject
						 TreeGraphNode altPredicate = findCompObject(dep2);
						 
						 if(!subj.label().tag().contains("NN") && ( nodeHash.containsKey(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT) || nodeHash.containsKey(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT)))
						 {
							 //log.debug("Came here");
							 subj = findComplementNoun(subj, dep2, EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
							 if(!subj.label().tag().contains("NN"))
							 {
								 //log.debug("XCalusal now");
								 subj = findCompSubject(dep2);
								 log.debug("MOD Triple: " + finalSubject + "<<>>" + predicate + "<<>>" + finalObject);
							 }
						 }		
						 						 
						 finalObject = getFullNoun(obj);
						 predicate = obj.value();
						 finalObject = findPrepNoun(obj);
						 finalSubject = getFullNoun(subj);

						 // find the negators for the predicates next
						 if(negHash.containsKey(pred + "")|| negHash.containsKey(altPredicate + ""))
							 predicate = "NOT " + predicate;
						
						 // find the relation for auxilliary
						 
						 // find the preparators next
						 
						 // fulcrum on the nsubj to see if there is an NNP in the vicinity - Sam - Yes
						 if(finalObject.indexOf(predicate) < 0 && predicate.indexOf(finalObject) < 0){
							// log.debug("Mod triple");
							 log.debug("MOD Triple	  " + finalSubject + "<<>>" + predicate + "<<>>" + finalObject);
						 }
						 temp.setObj1exp(finalSubject.toString());// part of future SetTriple
						 temp.setPredexp(predicate.toString());
						 temp.setObj2exp(finalObject.toString());
						 Triples.add(temp);
						 temp = new TripleWrapper();//be sure this line happens before you add another triple or they will point to the same thing
						
						 
						 //subjV.remove(subjIndex);
					 }
				 }
			 }
			 //nodeHash.put(subjR, subjV);
			 //nodeHash.put(objR, dobjV);
		 }
	 }
	 public TreeGraphNode findCompObject(TreeGraphNode subj)
	 {
		 TreeGraphNode retNode = subj;
		 Vector <TypedDependency> compVector = nodeHash.get(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT);
		 boolean subjFound = false;
		 if(compVector != null)
		 {
			 for(int cInd = 0;cInd < compVector.size()&& !subjFound;cInd++)
			 {
				 TypedDependency td = compVector.elementAt(cInd);
				 if(td.dep() == retNode)
				 {
					 retNode = td.gov();
					 subjFound = true;
				 }
			 }
		 }
		 
		 compVector = nodeHash.get(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
		 if(compVector != null)
		 {
			 subjFound = false;
			
			 for(int cInd = 0;cInd < compVector.size()&& !subjFound;cInd++)
			 {
				 TypedDependency td = compVector.elementAt(cInd);
				 if(td.dep() == retNode)
				 {
					 retNode = td.gov();
					 subjFound = true;
				 }
			 }
		 }
		 return retNode;
	 }
	 public TreeGraphNode findCompSubject(TreeGraphNode subj)
	 {
		 TreeGraphNode retNode = subj;
		 Vector <TypedDependency> compVector = nodeHash.get(EnglishGrammaticalRelations.XCLAUSAL_COMPLEMENT);
		 boolean subjFound = false;
		 if(compVector != null)
		 {
		 for(int cInd = 0;cInd < compVector.size()&& !subjFound;cInd++)
		 {
			 TypedDependency td = compVector.elementAt(cInd);
			 if(td.dep() == retNode)
			 {
				 retNode = td.gov();
				 subjFound = true;
			 }
		 }
		 compVector = nodeHash.get(EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
		 subjFound = false;
		 if(compVector !=null)
		 {
		 for(int cInd = 0;cInd < compVector.size()&& !subjFound;cInd++)
		 {
			 TypedDependency td = compVector.elementAt(cInd);
			 if(td.dep() == retNode)
			 {
				 retNode = td.gov();
				 subjFound = true;
			 }
		 }
		 compVector = nodeHash.get(EnglishGrammaticalRelations.NOMINAL_SUBJECT);
		 subjFound = false;
		 if(compVector != null){
		 for(int cInd = 0;cInd < compVector.size()&& !subjFound;cInd++)
		 {
			 TypedDependency td = compVector.elementAt(cInd);
			 if(td.gov() == retNode)
			 {
				 retNode = td.dep();
				 subjFound = true;
			 }
		 }
		 }
		 
		 return retNode;
		 }
		 }
		 return retNode;
	 }
//sometimes the DAMN complement is recursive
	  public TreeGraphNode findComplementNoun(TreeGraphNode subj,
			TreeGraphNode dep2, GrammaticalRelation relation) {

		 TreeGraphNode retNode = subj;
		 // find all the complements
		 // find the one where the dep is the same as dep passed through
		 // now find a nsubj based on that new gov
		 // start with CComplement
		 Vector <TypedDependency> compVector = nodeHash.get(relation);
		 if(compVector != null)
		 {
		 for(int cInd = 0;cInd < compVector.size();cInd++)
		 {
			 TypedDependency td = compVector.elementAt(cInd);
			 TreeGraphNode dep = td.dep();
			 TreeGraphNode gov = td.gov();
			 //log.debug("dep " + dep + "   G ov" + gov +" incoming matcher " + dep2);
			 
			 if(dep == dep2)
			 {
				 // now find the nsubj
				 //log.debug("Found Match");
				 Vector <TypedDependency> subjVector = nodeHash.get(EnglishGrammaticalRelations.NOMINAL_SUBJECT);
				 for(int subIndex = 0;subIndex < subjVector.size();subIndex++)
				 {
					 TypedDependency subTd = subjVector.elementAt(subIndex);
					 if(subTd.gov() == gov)
						 retNode = subTd.dep();
				 }
			 }
			 //compVector.remove(td);
		 }
		 return retNode;
		 }
		 return retNode;
	}
	  public String getFullNoun(TreeGraphNode node)
	 {
		 String finalObject = "";
		 boolean npFound = false;
		 TreeGraphNode parentSearcher = node;
		 while(!npFound)
		 {
			 //log.debug("Finder >> " + parentSearcher.labels());
			 if(!parentSearcher.label().toString().startsWith("NP"))
			 {
				 if(parentSearcher.parent() instanceof TreeGraphNode)
					 parentSearcher = (TreeGraphNode)parentSearcher.parent();
				 else
				 {
					 npFound = true;
					 parentSearcher = null;
					 //log.debug("Ending search");
				 }
			 }
			 else 
			{
				 npFound = true;
				 List<LabeledWord> lw = parentSearcher.labeledYield();
				 // if this is not a noun then I need find the actual proper noun
				 // and it may be because there is a CCOMP or XCOMP with this label
				 // or there is an amod with this label
				 for(int labIndex = 0;labIndex < lw.size();labIndex++)
				 {
					 finalObject = finalObject + lw.get(labIndex).word() + " ";
				 }
				 //log.debug();
			}
		 }
		 return finalObject;
	 }
	 public String findPreparator()
	 {
		 String retObject = "";
		 return retObject;
	 }
	 public void createNegations()
	 {
		 Vector <TypedDependency> negVector = nodeHash.get(EnglishGrammaticalRelations.NEGATION_MODIFIER);
		 if(negVector != null)
		 {
			 // run through each of these to see if I find any negation
			 for(int negIndex = 0;negIndex < negVector.size();negIndex++)
			 {
				 TypedDependency neg = negVector.elementAt(negIndex);
				 String gov = neg.gov() + "" ;
				 negHash.put(gov, gov);
			 }
		 }
	 }
	 public String findPrepNoun(TreeGraphNode noun)
	 {
		 // given the preperator
		 // complete the string
		 String retString = noun.value();
		 
		 if(!nodeHash.containsKey(EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER))
			 return retString;
		 Vector <TypedDependency> prepVector = nodeHash.get(EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
		 //prepVector.addAll(nodeHash.get(EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER));
		 for(int prepIndex = 0;prepIndex < prepVector.size();prepIndex++)
		 {
			 TypedDependency tdl = prepVector.elementAt(prepIndex);
			 TreeGraphNode gov = tdl.gov();
			 TreeGraphNode dep = tdl.dep();
			 if(noun == gov )
			 {
				 String fullNoun = getFullNoun(dep);
				 if(fullNoun.equalsIgnoreCase(dep.value()))
					 retString = retString + " " + tdl.reln().getSpecific() + " " + fullNoun + findPrepNoun(dep);
				 else
					 retString = retString + " " + tdl.reln().getSpecific() + " " + fullNoun + findPrepNoun(dep).replace(dep.value(), "");
			 }
		 }
		 return retString;
	 }
	 public String findPrepNounForPredicate(TreeGraphNode noun)
	 {
		 // given the preperator
		 // complete the string
		 String retString = "";
		 
		 if(!nodeHash.containsKey(EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER))
			 return retString;
		 Vector <TypedDependency> prepVector = nodeHash.get(EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
		 //prepVector.addAll(nodeHash.get(EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER));
		 for(int prepIndex = 0;prepIndex < prepVector.size();prepIndex++)
		 {
			 TypedDependency tdl = prepVector.elementAt(prepIndex);
			 TreeGraphNode gov = tdl.gov();
			 TreeGraphNode dep = tdl.dep();
			 if(noun == gov )
			 {				 
				 String fullNoun = getFullNoun(dep);
				 if(fullNoun.equalsIgnoreCase(dep.value()))
					 retString = retString + " " + tdl.reln().getSpecific() + " " + fullNoun + findPrepNoun(dep);
				 else
					 retString = retString + " " + tdl.reln().getSpecific() + " " + fullNoun;
				 //retString = retString + " " + tdl.reln().getSpecific() + " " + getFullNoun(dep) + findPrepNoun(dep);			 
			 }
			 }
		 return retString;
	 }
	
	
	 public void GetTriples(){
	//	 log.debug("IN GET TRIPLES");
		 // this.identifySubjandDobj(this.tdl);
		 this.createNegations();
		    // base case
		 //	log.debug("1");
		    this.findVerbTriples(EnglishGrammaticalRelations.NOMINAL_SUBJECT, EnglishGrammaticalRelations.DIRECT_OBJECT);
		    // passive case
		 //   log.debug("2");
		    this.findVerbTriples(EnglishGrammaticalRelations.AGENT, EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT);
		    // xsubj
		//    log.debug("3");
		    this.findVerbTriples(EnglishGrammaticalRelations.CONTROLLING_SUBJECT, EnglishGrammaticalRelations.DIRECT_OBJECT);
		    //samy test
		//    log.debug("4");
		    this.findVerbTriples(EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT, EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
		    // cops
		//    this.findModTriples(EnglishGrammaticalRelations.NOMINAL_SUBJECT, EnglishGrammaticalRelations.COPULA);
		    //log.debug("Number of DOBJ found " + test.dobjV.size() + "<<>>" + test.subjV.size());
		    // auxilliary
		//    this.findModTriples(EnglishGrammaticalRelations.NOMINAL_SUBJECT, EnglishGrammaticalRelations.AUX_MODIFIER);
		    // basic preps
	    this.findBaseSubjectTriples(EnglishGrammaticalRelations.NOMINAL_SUBJECT);
	}
	 public void SetupSheet()
	 {
		 //subject>predicate>object
		 RelationSheet temp = new RelationSheet("subjectofpredicate","subject","predicate");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 excelfiller.add(temp);
		 temp = new RelationSheet("predicateofobject","predicate","object");
		 
		 
		 
		  excelfiller.get(0).setRelation("subjectofpredicate");
		  excelfiller.get(0).setHeader1("subject");
		  excelfiller.get(0).setHeader2("predicate");
		  					
		  excelfiller.get(1).setRelation("predicateofobject");
		  excelfiller.get(1).setHeader1("predicate");
		  excelfiller.get(1).setHeader2("object");
		  
		  excelfiller.get(2).setRelation("objectofsubject");
		  excelfiller.get(2).setHeader1("object");
		  excelfiller.get(2).setHeader2("subject");
		  
		  excelfiller.get(3).setRelation("subjectexpanded");
		  excelfiller.get(3).setHeader1("subject");
		  excelfiller.get(3).setHeader2("expandedsubject");
		  
		  excelfiller.get(4).setRelation("predicateexpanded");
		  excelfiller.get(4).setHeader1("predicate");
		  excelfiller.get(4).setHeader2("expandedpredicate");
		  
		  excelfiller.get(5).setRelation("objectexpanded");
		  excelfiller.get(5).setHeader1("object");
		  excelfiller.get(5).setHeader2("expandedobject");
		  
		  excelfiller.get(6).setRelation("subjectpos");
		  excelfiller.get(6).setHeader1("subject");
		  excelfiller.get(6).setHeader2("pos");
		  
		  excelfiller.get(7).setRelation("predicatepos");
		  excelfiller.get(7).setHeader1("predicate");
		  excelfiller.get(7).setHeader2("pos");
		  
		  excelfiller.get(8).setRelation("objectpos");
		  excelfiller.get(8).setHeader1("object");
		  excelfiller.get(8).setHeader2("pos");
		  
		  excelfiller.get(9).setRelation("lemmaofpredicate");
		  excelfiller.get(9).setHeader1("predicate");
		  excelfiller.get(9).setHeader2("lemmatizedPred");
		  
		  excelfiller.get(10).setRelation("articleofsubject");
		  excelfiller.get(10).setHeader1("subject");
		  excelfiller.get(10).setHeader2("articlenum");
		  
		  excelfiller.get(11).setRelation("articleofpredicate");
		  excelfiller.get(11).setHeader1("predicate");
		  excelfiller.get(11).setHeader2("articlenum");
		  
		  excelfiller.get(12).setRelation("articleofobject");
		  excelfiller.get(12).setHeader1("object");
		  excelfiller.get(12).setHeader2("articlenum");
		  
		  excelfiller.get(13).setRelation("articleofsubjectexpanded");
		  excelfiller.get(13).setHeader1("expandedsubject");
		  excelfiller.get(13).setHeader2("articlenum");
		  
		  excelfiller.get(14).setRelation("articleofpredicateexpanded");
		  excelfiller.get(14).setHeader1("expandedpredicate");
		  excelfiller.get(14).setHeader2("articlenum");
		  
		  excelfiller.get(15).setRelation("articleofobjectexpanded");
		  excelfiller.get(15).setHeader1("expandedobject");
		  excelfiller.get(15).setHeader2("articlenum");
		  
		  //fill loader sheet
		  sheetToWriteOver = wb.createSheet("Loader");
		  rowToWriteOn = sheetToWriteOver.createRow(0);
			cellToWriteOn = rowToWriteOn.createCell(0);
			cellToWriteOn.setCellValue("SheetName");
			cellToWriteOn = rowToWriteOn.createCell(1);
			cellToWriteOn.setCellValue("Type");
		  for(int i = 0; i<excelfiller.size();i++){
			  rowToWriteOn = sheetToWriteOver.createRow(1+i);
				cellToWriteOn = rowToWriteOn.createCell(0);
				cellToWriteOn.setCellValue(excelfiller.get(i).getRelation());
				cellToWriteOn = rowToWriteOn.createCell(1);
				cellToWriteOn.setCellValue("Usual");
		  }
		  //create other sheets
		  for(int i = 0; i<excelfiller.size();i++){
			 //log.debug(wb.getNumberOfSheets());
			  sheetToWriteOver = wb.createSheet(excelfiller.get(i).getRelation());
			  //create approximate number of needed cells
			 
			  rowToWriteOn = sheetToWriteOver.createRow(0);
			  cellToWriteOn = rowToWriteOn.createCell(0);
			  cellToWriteOn.setCellValue("Relation");
			  cellToWriteOn = rowToWriteOn.createCell(1);
			  cellToWriteOn.setCellValue(excelfiller.get(i).getHeader1());
			  cellToWriteOn = rowToWriteOn.createCell(2);
			  cellToWriteOn.setCellValue(excelfiller.get(i).getHeader2());
			  
			 rowToWriteOn = sheetToWriteOver.createRow(1);
			  cellToWriteOn = rowToWriteOn.createCell(0);
			  cellToWriteOn.setCellValue(excelfiller.get(i).getRelation());
		  }
		  
		 
	 }
	 public void FillRow(String Sheetname, String col1, String col2){
		// sheetToWriteOver;// = wb.getSheet(Sheetname);
		 int i = 1;
		// log.debug(Sheetname);
		 	sheetToWriteOver = wb.getSheet(Sheetname);
		 	rowToWriteOn = sheetToWriteOver.getRow(1);
		 		if(rowToWriteOn.getCell(1) == null)
			 	cellToWriteOn = rowToWriteOn.createCell(1);
			 	else
			 		cellToWriteOn = rowToWriteOn.getCell(1);
		 if(cellToWriteOn.toString().length()==0){//if first column in the first line is empty
			cellToWriteOn = rowToWriteOn.createCell(1);
			cellToWriteOn.setCellValue(col1);
			cellToWriteOn = rowToWriteOn.createCell(2);
			cellToWriteOn.setCellValue(col2);
			i++;
		}else{
			while(!(sheetToWriteOver.getRow(i) == null)){
		 		i++;
		 	}
		 	rowToWriteOn = sheetToWriteOver.createRow(i);
		 	cellToWriteOn = rowToWriteOn.createCell(1);
		 	cellToWriteOn.setCellValue(col1);
			cellToWriteOn = rowToWriteOn.createCell(2);
			cellToWriteOn.setCellValue(col2);
		
		}
		
		
	 }
	 public void Fillexcel(String docout){
		 //fill data
		 for(int i = 0; i<Triples.size(); i++){
			 for(int j = 0; j<excelfiller.size(); j++){
				 if(excelfiller.get(j).getRelation().equals("subjectofpredicate")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj1(),Triples.get(i).getPred());
					}
				 if(excelfiller.get(j).getRelation().equals("predicateofobject")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPred(),Triples.get(i).getObj2());	
					}
				 //CHANGEDFORNEW
				 if(excelfiller.get(j).getRelation().equals("objectofsubject")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj2(),Triples.get(i).getObj1());	
					}
				 if(excelfiller.get(j).getRelation().equals("subjectexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj1(),Triples.get(i).getObj1exp());	
					}
				 if(excelfiller.get(j).getRelation().equals("predicateexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPred(),Triples.get(i).getPredexp());	
					}
				 if(excelfiller.get(j).getRelation().equals("objectexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj2(),Triples.get(i).getObj2exp());	
					}
				 if(excelfiller.get(j).getRelation().equals("subjectpos")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj1(),Triples.get(i).getObj1POS());	
					}
				 if(excelfiller.get(j).getRelation().equals("predicatepos")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPred(),Triples.get(i).getPredPOS()); 	
					}
				 if(excelfiller.get(j).getRelation().equals("objectpos")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj2(),Triples.get(i).getObj2POS());	
					}
				 if(excelfiller.get(j).getRelation().equals("lemmaofpredicate")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPred(),Triples.get(i).getNormPred());	
					}
				 if(excelfiller.get(j).getRelation().equals("articleofsubject")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj1(),Triples.get(i).getArticleNum());
					 }
				 if(excelfiller.get(j).getRelation().equals("articleofpredicate")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPred(),Triples.get(i).getArticleNum());
					 }
				 if(excelfiller.get(j).getRelation().equals("articleofobject")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj2(),Triples.get(i).getArticleNum());
					 }
				 if(excelfiller.get(j).getRelation().equals("articleofsubjectexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj1exp(),Triples.get(i).getArticleNum());
					 }
				 if(excelfiller.get(j).getRelation().equals("articleofpredicateexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getPredexp(),Triples.get(i).getArticleNum());
					 }
				 if(excelfiller.get(j).getRelation().equals("articleofobjectexpanded")){
					 FillRow(excelfiller.get(j).getRelation(),Triples.get(i).getObj2exp(),Triples.get(i).getArticleNum());
					 }
			 }
		 }
		 
		 
		 try {
			 log.debug("done");
		        FileOutputStream newExcelFile = new FileOutputStream(docout);
				wb.write(newExcelFile);
		        newExcelFile.close();  
		        log.debug("done");
			} catch (IOException e) {
				log.error( e );
			}
		
      log.debug("close");
	 }
}



