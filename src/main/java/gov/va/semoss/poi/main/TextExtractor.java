//http://stackoverflow.com/questions/6112419/handling-the-tab-character-in-java
//http://stackoverflow.com/questions/10250617/java-apache-poi-can-i-get-clean-text-from-ms-word-doc-files
package gov.va.semoss.poi.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.xml.sax.ContentHandler;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.log4j.Logger;

class TextExtractor {

  private static final Logger log = Logger.getLogger( TextExtractor.class );

  private final OutputStream outputstream;
  private final ParseContext context;
  private final Detector detector;
  private final Parser parser;
  private final Metadata metadata;
  private String extractedText;

  public TextExtractor() {
    context = new ParseContext();
    detector = new DefaultDetector();
    parser = new AutoDetectParser( detector );
    context.set( Parser.class, parser );
    outputstream = new ByteArrayOutputStream();
    metadata = new Metadata();
  }

  public void process( String filename ) throws Exception {
    URL url;
    File file = new File( filename );
    if ( file.isFile() ) {
      url = file.toURI().toURL();
    }
    else {
      url = new URL( filename );
    }
    
    try (InputStream input = TikaInputStream.get( url, metadata )) {
      ContentHandler handler = new BodyContentHandler( outputstream );
      parser.parse( input, handler, metadata, context );
    }
  }

  public String getString() throws IOException {
    //Get the text into a String object
    extractedText = outputstream.toString();
    extractedText = extractedText.replace( "\n", " @ " ).replace( "\r", " " );
    //Do whatever you want with this String object.
    log.debug( "Extractedtext " + extractedText );
    //      String docname = "PKrequest\\PKuseCase.txt";
    //	log.debug(docname);
    //	FileOutputStream out = new FileOutputStream(docname);
    //	extractedText = extractedText.replace("\t",".");
    //	out.write(extractedText.getBytes());
    //	out.close();

    return extractedText;
  }

  public String WebsiteTextExtractor( String docin ) throws Exception {

    final String url = docin;
    boolean knownwebsite = false;
    org.jsoup.nodes.Document doc = Jsoup.connect( url ).get();
    String extractedtext = "";

    if ( url.contains( "nytimes.com" ) && false ) {
      knownwebsite = true;
      for ( Element element : doc.select( "p.story-body-text" ) ) {
        if ( element.hasText() ) { // Skip those tags without text
          log.debug( "This is element text" + element.text() );
          extractedtext = extractedtext.concat( element.text() );
        }
      }
    }
    if ( knownwebsite ) {
      log.debug( "NON USED WEB READER" );
      extractedtext = doc.text();
    }
    if ( !knownwebsite ) {
      log.debug( "USED WEB READER" );
      URL urlobj = new URL( url );
      // NOTE: Use ArticleExtractor unless DefaultExtractor gives better results for you   
      String text = ArticleExtractor.INSTANCE.getText( urlobj );
      extractedtext = text;
      log.debug( text );
    }
    //	}
    extractedtext = extractedtext.replace( "\n", " @ " ).replace( "\r", " " );
    log.debug( "extracted text being sent back" + extractedtext );
    return extractedtext;

  }

  public String WorddocTextExtractor( String docin ) throws Exception {
    TextExtractor textExtractor = new TextExtractor();
    textExtractor.process( docin );
    return textExtractor.getString();
  }

  public String MasterResumeExtractor( String docin ) throws Exception {
    TextExtractor textExtractor = new TextExtractor();
    textExtractor.process( docin );
    return textExtractor.getString();
  }
}
