package zds.parseTuto;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestJSoup {
    private static List<String>  listeFichiersTutos        = new ArrayList<String>();
    private static List<String>  listeCheminsFichiersTutos = new ArrayList<String>();
    private static final Pattern ZCODE_MATH                = Pattern.compile( "(<math>)(.+?)(</math>)" );
    private static final String  FOLDER_TUTO_ENTER         = "/Users/gaowenjia/work/tutos_ths/tutos_sdzv3/Sources/";

    public static void main( String[] args ) throws SAXException, IOException, ParserConfigurationException,
            XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
        File folder = new File( FOLDER_TUTO_ENTER );
        listFilesForFolder( folder );

        for ( String fileStr : listeCheminsFichiersTutos ) {
            File xmlFile = new File( fileStr );
            System.out.println( "#### " + fileStr + " ####" );

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse( xmlFile );
            doc.getDocumentElement().normalize();
            String buffer;

            NodeList nodes = doc.getElementsByTagName( "introduction" );
            org.w3c.dom.Element child;
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            nodes = doc.getElementsByTagName( "conclusion" );
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            nodes = doc.getElementsByTagName( "texte" );
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            nodes = doc.getElementsByTagName( "label" );
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            nodes = doc.getElementsByTagName( "reponse" );
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            nodes = doc.getElementsByTagName( "explication" );
            for ( int i = 0; i < nodes.getLength(); i++ ) {
                child = (org.w3c.dom.Element) nodes.item( i );
                buffer = cleanZcodeSource( child.getTextContent() );
                buffer = zCodeToMarkdown( buffer );
                child.setTextContent( buffer );
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, "introduction conclusion texte label reponse explication" );
            transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            Result output = new StreamResult( new File( fileStr ) );
            Source input = new DOMSource( doc );

            transformer.transform( input, output );
        }

    }

    /*
     * Méthode de conversion de toutes les balises du zCode vers leur équivalent en syntaxe markdown. S'appuie sur le parseur HTML JSoup.
     * 
     * Les multiples appels à JSoup.parse() sont effectués afin de traiter correctement les balises imbriquées : sans ce retour à zéro
     * systématique, le parseur n'agirait pas sur les contenus déjà parsés dans une boucle antérieure.
     * 
     * Les appels à escapeHtmlContent() sont systématiques eux-aussi, pour éviter que le parseur ne vienne fourrer son nez dans le contenu
     * des balises <code> et <minicode> à chaque nouveau parsage.
     * 
     * Malgré la conception baclée et les multiples itérations, les perfs sont très bonnes en comparaison à des traitements par regex. =)
     */
    public static String zCodeToMarkdown( String contenu ) {

        contenu = escapeZcodeHtmlContent( contenu );
        // conversion <math>
        Matcher matcher = ZCODE_MATH.matcher( contenu );
        String contenuBalise = "";
        StringBuffer sb = new StringBuffer();
        while ( matcher.find() ) {
            contenuBalise = matcher.group( 2 );
            contenuBalise = contenuBalise.replaceAll( "\\\\usepackage\\{.+?\\}", "" );
            matcher.appendReplacement( sb, Matcher.quoteReplacement( "$" + contenuBalise + "$" ) );
            contenuBalise = "";
        }
        matcher.appendTail( sb );

        Document document = Jsoup.parse( sb.toString(), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <lien>
        // et reconstruction des liens wikipedia fr, wikipedia en et doc PHP
        for ( Element elt : document.select( "lien" ) ) {

            if ( elt.hasAttr( "doc" ) && elt.attr( "doc" ).equals( "php" ) && elt.hasAttr( "url" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + "http://php.net/"
                        + elt.attr( "url" ) + ")", "" ) );
            } else if ( elt.hasAttr( "doc" ) && elt.attr( "doc" ).equals( "php" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + "http://php.net/" + elt.text()
                        + ")", "" ) );
            } else if ( elt.hasAttr( "type" ) && elt.attr( "type" ).equals( "wikipedia" ) &&
                    elt.hasAttr( "langue" ) && elt.attr( "langue" ).equals( "en" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + "http://en.wikipedia.org/wiki/"
                        + elt.attr( "url" )
                        + ")", "" ) );
            } else if ( elt.hasAttr( "type" ) && elt.attr( "type" ).equals( "wikipedia" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + "http://fr.wikipedia.org/wiki/"
                        + elt.attr( "url" )
                        + ")", "" ) );
            } else if ( elt.hasAttr( "url" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + elt.attr( "url" ) + ")", "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](" + elt.html() + ")", "" ) );
            }
        }

        // conversion <email>
        for ( Element elt : document.select( "email" ) ) {
            if ( elt.hasAttr( "nom" ) ) {
                elt.replaceWith( TextNode.createFromEncoded(
                        "[" + elt.html() + "](mailto:" + elt.attr( "nom" ) + ")", "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( "[" + elt.html() + "](mailto:" + elt.html() + ")", "" ) );
            }
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <acronyme>
        // il faut d'abord enregistrer les définitions dans une Map
        // TODO: à modifier, deux acronymes de même sigles dans un même tuto vont s'écraser dans la HashMap.
        Map<String, String> acronymes = new HashMap<String, String>();
        for ( Element elt : document.select( "acronyme" ) ) {
            acronymes.put( elt.text(), elt.attr( "valeur" ) );
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <indice>
        for ( Element elt : document.select( "indice" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "~" + elt.html() + "~", "" ) );
        }

        // conversion <exposant>
        for ( Element elt : document.select( "exposant" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "^" + elt.html() + "^", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <touche>
        for ( Element elt : document.select( "touche" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "||" + elt.html() + "||", "" ) );
        }

        // conversion <image>
        for ( Element elt : document.select( "image" ) ) {
            if ( elt.hasAttr( "legende" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( "![" + elt.attr( "legende" ) + "](" + elt.html() + ")",
                        "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( "![](" + elt.html() + ")", "" ) );
            }
        }

        // conversion <video>
        for ( Element elt : document.select( "video" ) ) {
            if ( elt.text().contains( ".youtube.com" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
            } else if ( elt.text().contains( ".dailymotion.com" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( "![video](" + elt.html() + ")", "" ) );
            }
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <italique>
        for ( Element elt : document.select( "italique" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "_" + elt.html() + "_", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <gras>
        for ( Element elt : document.select( "gras" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "**" + elt.html() + "**", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <barre>
        for ( Element elt : document.select( "barre" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "~~" + elt.html() + "~~", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <souligne>
        for ( Element elt : document.select( "souligne" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <titre1>
        for ( Element elt : document.select( "titre1" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "\n# " + elt.text() + "\n", "" ) );
        }

        // conversion <titre2>
        for ( Element elt : document.select( "titre2" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "\n## " + elt.text() + "\n", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <couleur>
        for ( Element elt : document.select( "couleur" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <taille>
        for ( Element elt : document.select( "taille" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <police>
        for ( Element elt : document.select( "police" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <liste>
        // détection des sous-listes à six niveaux max
        // (tant pis pour les gros marteaux qu'ont fait des listes à plus de six niveaux)
        for ( Element liste : document.select( "liste liste liste liste liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "                    1. " + puce.html() + "\n", "" ) );
                } else {
                    puce.replaceWith( new DataNode( "                    - " + puce.html() + "\n", "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        for ( Element liste : document.select( "liste liste liste liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "                1. " + puce.html() + "\n", "" ) );
                } else {
                    puce.replaceWith( new DataNode( "                - " + puce.html() + "\n", "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        for ( Element liste : document.select( "liste liste liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "            1. " + puce.html() + "\n", "" ) );
                } else {
                    puce.replaceWith( new DataNode( "            - " + puce.html() + "\n", "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        for ( Element liste : document.select( "liste liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "        1. " + puce.html() + "\n", "" ) );
                } else {
                    puce.replaceWith( new DataNode( "        - " + puce.html() + "\n", "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        // détection des sous-listes
        for ( Element liste : document.select( "liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "    1. " + puce.html() + "\n", "" ) );
                } else {
                    puce.replaceWith( new DataNode( "    - " + puce.html() + "\n", "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        // détection des listes
        for ( Element liste : document.select( "liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "\n1. " + puce.html(), "" ) );
                } else {
                    puce.replaceWith( new DataNode( "\n- " + puce.html(), "" ) );
                }
            }
            liste.replaceWith( new DataNode( "\n" + liste.data() + "\n\n", "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <citation>
        String[] linesTemp;
        String bufferTemp = "";
        for ( Element elt : document.select( "citation" ) ) {
            if ( elt.hasAttr( "nom" ) ) {
                bufferTemp += "\n=[" + elt.attr( "nom" ) + "]";
                if ( elt.hasAttr( "lien" ) ) {
                    bufferTemp += "(" + elt.attr( "lien" ) + ")";
                }
            }
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n> " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( bufferTemp, "" ) );
            bufferTemp = "";
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <question>
        bufferTemp = "";
        for ( Element elt : document.select( "question" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n| " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( "\n[[question]]" + bufferTemp, "" ) );

            bufferTemp = "";
        }

        // conversion <attention>
        bufferTemp = "";
        for ( Element elt : document.select( "attention" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n| " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( "\n[[attention]]" + bufferTemp, "" ) );

            bufferTemp = "";
        }

        // conversion <erreur>
        bufferTemp = "";
        for ( Element elt : document.select( "erreur" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n| " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( "\n[[erreur]]" + bufferTemp, "" ) );

            bufferTemp = "";
        }

        // conversion <information>
        bufferTemp = "";
        for ( Element elt : document.select( "information" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n| " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( "\n[[information]]" + bufferTemp, "" ) );

            bufferTemp = "";
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <secret>
        bufferTemp = "";
        for ( Element elt : document.select( "secret" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n| " + line;
            }
            bufferTemp += "\n";
            elt.replaceWith( new DataNode( "\n[[secret]]" + bufferTemp, "" ) );

            bufferTemp = "";
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <position>
        for ( Element elt : document.select( "position" ) ) {
            elt.replaceWith( new DataNode( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <position> (deuxième passe pour les fous de l'alignement imbriqué)
        for ( Element elt : document.select( "position" ) ) {
            elt.replaceWith( new DataNode( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <flottant>
        for ( Element elt : document.select( "flottant" ) ) {
            elt.replaceWith( new DataNode( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // nettoyage <tableau> pour les tarés qui ont aligné des trucs au sein d'une cellule...
        for ( Element tableau : document.select( "tableau" ) ) {
            for ( Element ligne : tableau.getElementsByTag( "ligne" ) ) {
                for ( Element cellule : ligne.children() ) {
                    for ( Element position : cellule.getElementsByTag( "position" ) ) {
                        position.replaceWith( new DataNode( position.html(), "" ) );
                    }
                }
            }
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // nettoyage colspan <tableau>
        for ( Element tableau : document.select( "tableau" ) ) {
            for ( Element ligne : tableau.getElementsByTag( "ligne" ) ) {
                for ( Element cellule : ligne.children() ) {
                    if ( cellule.hasAttr( "fusion_col" ) ) {
                        int xspan = Integer.valueOf( cellule.attr( "fusion_col" ) );
                        cellule.removeAttr( "fusion_col" );
                        for ( int i = 1; i < xspan; i++ ) {
                            cellule.after( cellule.clone() );
                        }
                    }
                }
            }
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // nettoyage rowspan <tableau>
        for ( Element tableau : document.select( "tableau" ) ) {
            Element ligne;
            for ( int l = 0; l < tableau.getElementsByTag( "ligne" ).size(); l++ ) {
                ligne = tableau.getElementsByTag( "ligne" ).get( l );
                Element cellule;
                for ( int c = 0; c < ligne.children().size(); c++ ) {
                    cellule = ligne.children().get( c );
                    if ( cellule.hasAttr( "fusion_lig" ) ) {
                        int yspan = Integer.valueOf( cellule.attr( "fusion_lig" ) );
                        cellule.removeAttr( "fusion_lig" );
                        for ( int i = l + 1; i < l + yspan; i++ ) {
                            if ( c > 0 ) {
                                tableau.getElementsByTag( "ligne" ).get( i ).children().get( c - 1 )
                                        .after( cellule.clone() );
                            } else {
                                tableau.getElementsByTag( "ligne" ).get( i ).children().get( c )
                                        .before( cellule.clone() );
                            }
                        }
                    }
                }
            }
        }

        document = Jsoup.parse( escapeZcodeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <code>
        for ( Element elt : document.select( "code" ) ) {
            String optionsBuffer = "";
            String legende = "";
            if ( elt.hasAttr( "type" ) ) {
                optionsBuffer += elt.attr( "type" );
            }
            if ( elt.hasAttr( "surligne" ) ) {
                optionsBuffer += " hl_lines=\"" + elt.attr( "surligne" ).replace( ",", " " ) + "\"";
            }
            if ( elt.hasAttr( "debut" ) ) {
                optionsBuffer += " linenostart=\"" + elt.attr( "debut" ) + "\"";
            }
            if ( elt.hasAttr( "titre" ) ) {
                legende = elt.attr( "titre" );
            }

            if ( !"".equals( legende ) ) {
                elt.replaceWith( new DataNode( "\n```" + optionsBuffer + "\n" + elt.html() + "\n```\n" + "Code:"
                        + legende + "\n", "" ) );
            } else {
                elt.replaceWith( new DataNode( "\n```" + optionsBuffer + "\n" + elt.html() + "\n```\n", "" ) );
            }
        }

        // conversion <minicode>
        for ( Element elt : document.select( "minicode" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "`" + elt.html() + "`", "" ) );
        }

        document = Jsoup.parse( escapeMarkdownHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <tableau>
        for ( Element tableau : document.select( "tableau" ) ) {

            // MEGA try-catch pour mettre de côté les tutos qui contiennent des tableaux foireux (merci Taguan et ses tableaux non
            // tabulaires ! xD)
            try {
                String legendeTableau = "";
                if ( !tableau.getElementsByTag( "legende" ).isEmpty() ) {
                    legendeTableau = tableau.getElementsByTag( "legende" ).get( 0 ).text();
                    tableau.getElementsByTag( "legende" ).get( 0 ).remove();
                }

                int largeurTableau = 0;
                // on calcule la largeur du tableau en se basant sur sa première ligne
                int i = 0;
                Elements cellules = tableau.getElementsByTag( "ligne" ).first().getElementsByTag( "cellule" );
                while ( cellules.isEmpty() ) {
                    i++;
                    cellules = tableau.getElementsByTag( "ligne" ).get( i ).getElementsByTag( "cellule" );
                }
                largeurTableau = cellules.size();

                int hauteurTableau = tableau.getElementsByTag( "ligne" ).size();

                // par défaut, on considère qu'il n'y a pas d'en-têtes
                boolean hasEntete = false;

                // si des en-tête sont définies
                if ( !tableau.getElementsByTag( "entete" ).isEmpty() ) {
                    hasEntete = true;
                }

                Cellule[][] data = new Cellule[hauteurTableau][largeurTableau];

                for ( int k = 0; k < hauteurTableau; k++ ) {
                    for ( int j = 0; j < largeurTableau; j++ ) {
                        data[k][j] = new Cellule();
                    }
                }

                i = 0;
                cellules = tableau.getElementsByTag( "ligne" ).first().getElementsByTag( "entete" );
                while ( cellules.isEmpty() && i < tableau.getElementsByTag( "ligne" ).size() ) {
                    cellules = tableau.getElementsByTag( "ligne" ).get( i ).getElementsByTag( "entete" );
                    i++;
                }

                if ( !cellules.isEmpty() ) {
                    hasEntete = true;
                    // Tableau avec en-têtes
                    i = 0;
                    for ( Element cellule : cellules ) {
                        int cellMaxWidth = 0;
                        for ( String cellInnerLine : cellule.html().split( "\r\n|\n" ) ) {
                            data[0][i].texte.add( cellInnerLine );
                            if ( cellInnerLine.length() > cellMaxWidth ) {
                                cellMaxWidth = cellInnerLine.length();
                            }
                        }
                        data[0][i].largeur = cellMaxWidth;
                        // System.out.println( String.format( "data[%d][%d] = %s", 0, i, data[0][i] ) );
                        i++;
                    }
                }

                int indexLigne = hasEntete ? 1 : 0;
                for ( Element ligne : tableau.getElementsByTag( "ligne" ) ) {
                    cellules = ligne.getElementsByTag( "cellule" );
                    if ( !cellules.isEmpty() ) {
                        i = 0;
                        for ( Element cellule : cellules ) {
                            int cellMaxWidth = 0;
                            for ( String cellInnerLine : cellule.html().split( "\r\n|\n" ) ) {
                                data[indexLigne][i].texte.add( cellInnerLine );
                                if ( cellInnerLine.length() > cellMaxWidth ) {
                                    cellMaxWidth = cellInnerLine.length();
                                }
                            }
                            data[indexLigne][i].largeur = cellMaxWidth;
                            // System.out.println( String.format( "data[%d][%d] = %s", indexLigne, i, data[indexLigne][i] ) );
                            i++;
                        }
                        indexLigne++;
                    }
                }

                // parcours par ligne
                for ( int k = 0; k < hauteurTableau; k++ ) {
                    int rowMaxHeight = 0;
                    // determination de la hauteur max de la ligne
                    for ( int j = 0; j < largeurTableau; j++ ) {
                        if ( data[k][j].texte.size() > rowMaxHeight ) {
                            rowMaxHeight = data[k][j].texte.size();
                        }
                    }
                    // complétion de toutes les cellules de la ligne avec des lignes vides
                    for ( int j = 0; j < largeurTableau; j++ ) {
                        while ( data[k][j].texte.size() < rowMaxHeight ) {
                            data[k][j].texte.add( "" );
                        }
                    }

                }

                // parcours par colonne
                for ( int k = 0; k < largeurTableau; k++ ) {
                    int columnMaxWidth = 0;
                    // determination de la largeur max de la colonne
                    for ( int j = 0; j < hauteurTableau; j++ ) {
                        if ( data[j][k].largeur > columnMaxWidth ) {
                            columnMaxWidth = data[j][k].largeur;
                        }
                    }
                    // complétion de toutes les lignes de textes de la colonne avec des espaces
                    for ( int j = 0; j < hauteurTableau; j++ ) {
                        data[j][k].largeur = columnMaxWidth;

                        ListIterator<String> listIterator = data[j][k].texte.listIterator();
                        while ( listIterator.hasNext() ) {
                            String lineTemp = listIterator.next();
                            int offset = columnMaxWidth - lineTemp.length();
                            for ( int o = 0; o < offset; o++ ) {
                                lineTemp += " ";
                            }
                            listIterator.set( lineTemp );
                        }

                    }
                }

                // dessin du tableau, à base de popopopop...
                String tableauEntier = "";

                // si tableau avec en-têtes
                if ( hasEntete ) {
                    // dessin bordure supérieure
                    tableauEntier += "+";
                    for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                        for ( int cursor = 0; cursor < data[0][colonneIndex].largeur; cursor++ ) {
                            tableauEntier += "-";
                        }
                        tableauEntier += "+";
                    }
                    tableauEntier += "\n";

                    // dessin row
                    int curLines = 0;
                    while ( curLines < data[0][0].texte.size() ) {
                        tableauEntier += "|";
                        for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                            tableauEntier += data[0][colonneIndex].texte.get( curLines );
                            tableauEntier += "|";
                        }
                        tableauEntier += "\n";
                        curLines++;
                    }

                    // dessin bordure inférieure
                    tableauEntier += "+";
                    for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                        for ( int cursor = 0; cursor < data[0][colonneIndex].largeur; cursor++ ) {
                            tableauEntier += "=";
                        }
                        tableauEntier += "+";
                    }
                    tableauEntier += "\n";
                    hasEntete = true;

                }

                for ( int ligneIndex = hasEntete ? 1 : 0; ligneIndex < hauteurTableau; ligneIndex++ ) {
                    if ( !hasEntete ) {
                        // dessin bordure supérieure
                        tableauEntier += "+";
                        for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                            for ( int cursor = 0; cursor < data[ligneIndex][colonneIndex].largeur; cursor++ ) {
                                tableauEntier += "-";
                            }
                            tableauEntier += "+";
                        }
                        tableauEntier += "\n";
                    }

                    // pour dessiner les bordures ensuite
                    hasEntete = false;

                    // dessin row
                    int curLines = 0;
                    while ( curLines < data[ligneIndex][0].texte.size() ) {
                        tableauEntier += "|";
                        for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                            tableauEntier += data[ligneIndex][colonneIndex].texte.get( curLines );
                            tableauEntier += "|";
                        }
                        tableauEntier += "\n";
                        curLines++;
                    }
                }
                tableauEntier += "+";
                for ( int colonneIndex = 0; colonneIndex < largeurTableau; colonneIndex++ ) {
                    for ( int cursor = 0; cursor < data[0][colonneIndex].largeur; cursor++ ) {
                        tableauEntier += "-";
                    }
                    tableauEntier += "+";
                }
                tableauEntier += "\n";

                // System.out.println( "Tableau: " + largeurTableau + "x" + hauteurTableau );
                // System.out.println( tableauEntier );

                if ( !"".equals( legendeTableau ) ) {
                    tableau.replaceWith( new DataNode( "\n\n" + tableauEntier + "Table:" + legendeTableau + "\n\n", "" ) );
                } else {
                    tableau.replaceWith( new DataNode( "\n\n" + tableauEntier + "\n\n", "" ) );
                }
            } catch ( Exception e ) {
                System.out.println( "/!\\ Erreur de parsing d'un tableau dans le tuto /!\\\n" + tableau.html() + "\n" );
            }
        }

        // conversion <acronyme> (suite)
        // on ressort tout le document dans une string
        contenu = document.toString();
        // on saute deux lignes en toute fin de document
        contenu += "\n\n";
        // et on recrache les acronymes stockés
        for ( Map.Entry<String, String> item : acronymes.entrySet() )
        {
            contenu += "*[" + item.getKey() + "]: " + item.getValue() + "\n";
        }

        // "dénettoyage" des entitiés HTML cleanées par JSoup
        // NOTE : inutile depuis les modifs apportées sur la classe org.jsoup.nodes.Entities
        // contenu = contenu.replace( "&apos;", "'" );
        // contenu = contenu.replace( "&quot;", "\"" );
        // contenu = contenu.replace( "&amp;", "&" );
        // contenu = contenu.replace( "&gt;", ">" );
        // contenu = contenu.replace( "&lt;", "<" );

        return contenu;
    }

    /*
     * Méthode helper pour la lecture d'un fichier.
     */
    public static String readFile( String filename ) {
        String content = null;
        File file = new File( filename );
        try {
            FileReader reader = new FileReader( file );
            char[] chars = new char[(int) file.length()];
            reader.read( chars );
            content = new String( chars );
            reader.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return content;
    }

    /*
     * Méthode helper pour le listage des fichiers d'un répertoire.
     */
    public static void listFilesForFolder( final File folder ) {
        for ( final File fileEntry : folder.listFiles() ) {
            if ( fileEntry.isDirectory() ) {
                listFilesForFolder( fileEntry );
            } else {
                listeFichiersTutos.add( fileEntry.getName() );
                if ( !fileEntry.getAbsolutePath().contains( "metadata.xml" ) && fileEntry.getAbsolutePath().contains( ".xml" ) ) {
                    listeCheminsFichiersTutos.add( fileEntry.getAbsolutePath() );
                }
            }
        }
    }

    /*
     * Méthode d'échappement des chevrons < et > contenus au sein des sections <code> et <minicode>, pour que JSoup ne cherche pas à
     * corriger les balises HTML-like non fermées qu'elles peuvent éventuellement contenir (exemple : sans ce traitement, le code Java
     * "List<String>" deviendrait "List<string></string>" ...).
     */
    public static String escapeZcodeHtmlContent( String contenu ) {
        final Pattern ZCODE_CODE = Pattern.compile( "(<code([^>]*?)>)(.+?)(</code>)", Pattern.DOTALL );
        final Pattern ZCODE_MINICODE = Pattern.compile( "(<minicode([^>]*?)>)(.+?)(</minicode>)", Pattern.DOTALL );

        Matcher matcher = ZCODE_CODE.matcher( contenu );
        String contenuBalise = "";
        StringBuffer sb = new StringBuffer();
        while ( matcher.find() ) {
            contenuBalise = matcher.group( 3 );
            contenuBalise = contenuBalise.replace( "<", "&lt;" );
            contenuBalise = contenuBalise.replace( ">", "&gt;" );
            matcher.appendReplacement( sb,
                    Matcher.quoteReplacement( matcher.group( 1 ) + contenuBalise + matcher.group( 4 ) ) );
            contenuBalise = "";
        }
        matcher.appendTail( sb );

        matcher = ZCODE_MINICODE.matcher( sb.toString() );
        contenuBalise = "";
        StringBuffer sb2 = new StringBuffer();
        while ( matcher.find() ) {
            contenuBalise = matcher.group( 3 );
            contenuBalise = contenuBalise.replace( "<", "&lt;" );
            contenuBalise = contenuBalise.replace( ">", "&gt;" );
            matcher.appendReplacement( sb2,
                    Matcher.quoteReplacement( matcher.group( 1 ) + contenuBalise + matcher.group( 4 ) ) );
            contenuBalise = "";
        }
        matcher.appendTail( sb2 );

        return sb2.toString();
    }

    /*
     * Méthode d'échappement des chevrons < et > contenus au sein des sections ``` et `, pour que JSoup ne cherche pas à corriger les
     * balises HTML-like non fermées qu'elles peuvent éventuellement contenir (exemple : sans ce traitement, le code Java "List<String>"
     * deviendrait "List<string></string>" ...).
     */
    public static String escapeMarkdownHtmlContent( String contenu ) {
        final Pattern MD_CODE = Pattern.compile( "(```)(.+?)(```)", Pattern.DOTALL );
        final Pattern MD_MINICODE = Pattern.compile( "(`)(.+?)(`)" );

        Matcher matcher = MD_CODE.matcher( contenu );
        String contenuBalise = "";
        StringBuffer sb = new StringBuffer();
        while ( matcher.find() ) {
            contenuBalise = matcher.group( 2 );
            contenuBalise = contenuBalise.replace( "<", "&lt;" );
            contenuBalise = contenuBalise.replace( ">", "&gt;" );
            matcher.appendReplacement( sb,
                    Matcher.quoteReplacement( matcher.group( 1 ) + contenuBalise + matcher.group( 3 ) ) );
            contenuBalise = "";
        }
        matcher.appendTail( sb );

        matcher = MD_MINICODE.matcher( sb.toString() );
        contenuBalise = "";
        StringBuffer sb2 = new StringBuffer();
        while ( matcher.find() ) {
            contenuBalise = matcher.group( 2 );
            contenuBalise = contenuBalise.replace( "<", "&lt;" );
            contenuBalise = contenuBalise.replace( ">", "&gt;" );
            matcher.appendReplacement( sb2,
                    Matcher.quoteReplacement( matcher.group( 1 ) + contenuBalise + matcher.group( 3 ) ) );
            contenuBalise = "";
        }
        matcher.appendTail( sb2 );

        return sb2.toString();
    }

    /*
     * Méthode de nettoyage de la source, pour que les espaces mangés en début ou fin de balises zCode soient restitués respectivement avant
     * ou après les balises. C'est nécessaire, car le parseur HTML utilisé derrière ne prend logiquement pas en comtpe ces espaces en début
     * et fin de balise, et comme le zCode a souvent été mis n'importe comment pas les auteurs, ça pourrait causer la suppression
     * indésirable d'espaces et/ou de sauts de lignes.
     */
    public static String cleanZcodeSource( String contenu ) {
        // Correction des balises simples où le zCode a bouffé l'espace d'avant ou d'après
        contenu = contenu.replaceAll( "<gras>(\\s+)", "$1<gras>" );
        contenu = contenu.replaceAll( "(\\s+)</gras>", "</gras>$1" );
        contenu = contenu.replaceAll( "<italique>(\\s+)", "$1<italique>" );
        contenu = contenu.replaceAll( "(\\s+)</italique>", "</italique>$1" );
        contenu = contenu.replaceAll( "<barre>(\\s+)", "$1<barre>" );
        contenu = contenu.replaceAll( "(\\s+)</barre>", "</barre>$1" );
        contenu = contenu.replaceAll( "<souligne>(\\s+)", "$1<souligne>" );
        contenu = contenu.replaceAll( "(\\s+)</souligne>", "</souligne>$1" );
        contenu = contenu.replaceAll( "<indice>(\\s+)", "$1<indice>" );
        contenu = contenu.replaceAll( "(\\s+)</indice>", "</indice>$1" );
        contenu = contenu.replaceAll( "<exposant>(\\s+)", "$1<exposant>" );
        contenu = contenu.replaceAll( "(\\s+)</exposant>", "</exposant>$1" );
        contenu = contenu.replaceAll( "<math>(\\s+)", "$1<math>" );
        contenu = contenu.replaceAll( "(\\s+)</math>", "</math>$1" );
        contenu = contenu.replaceAll( "<titre1>(\\s+)", "$1<titre1>" );
        contenu = contenu.replaceAll( "(\\s+)</titre1>", "</titre1>$1" );
        contenu = contenu.replaceAll( "<titre2>(\\s+)", "$1<titre2>" );
        contenu = contenu.replaceAll( "(\\s+)</titre2>", "</titre2>$1" );
        contenu = contenu.replaceAll( "<touche>(\\s+)", "$1<touche>" );
        contenu = contenu.replaceAll( "(\\s+)</touche>", "</touche>$1" );
        contenu = contenu.replaceAll( "<question>(\\s+)", "$1<question>" );
        contenu = contenu.replaceAll( "(\\s+)</question>", "</question>$1" );
        contenu = contenu.replaceAll( "<information>(\\s+)", "$1<information>" );
        contenu = contenu.replaceAll( "(\\s+)</information>", "</information>$1" );
        contenu = contenu.replaceAll( "<attention>(\\s+)", "$1<attention>" );
        contenu = contenu.replaceAll( "(\\s+)</attention>", "</attention>$1" );
        contenu = contenu.replaceAll( "<erreur>(\\s+)", "$1<erreur>" );
        contenu = contenu.replaceAll( "(\\s+)</erreur>", "</erreur>$1" );
        contenu = contenu.replaceAll( "<liste>\\s*", "<liste>" );
        contenu = contenu.replaceAll( "\\s*</liste>", "</liste>" );
        contenu = contenu.replaceAll( "\\s*<puce>\\s*", "<puce>" );
        contenu = contenu.replaceAll( "\\s*</puce>\\s*", "</puce>" );
        contenu = contenu.replaceAll( "<tableau>(\\s+)", "$1<tableau>" );
        contenu = contenu.replaceAll( "(\\s+)</tableau>", "</tableau>$1" );

        // Correction des balises complexes où le zCode a bouffé l'espace d'avant ou d'après
        contenu = contenu.replaceAll( "<couleur([^>]*?)>(\\s+)", "$2<couleur$1>" );
        contenu = contenu.replaceAll( "(\\s+)</couleur>", "</couleur>$1" );
        contenu = contenu.replaceAll( "<taille([^>]*?)>(\\s+)", "$2<taille$1>" );
        contenu = contenu.replaceAll( "(\\s+)</taille>", "</taille>$1" );
        contenu = contenu.replaceAll( "<police([^>]*?)>(\\s+)", "$2<police$1>" );
        contenu = contenu.replaceAll( "(\\s+)</police>", "</police>$1" );
        contenu = contenu.replaceAll( "<flottant([^>]*?)>(\\s+)", "$2<flottant$1>" );
        contenu = contenu.replaceAll( "(\\s+)</flottant>", "</flottant>$1" );
        contenu = contenu.replaceAll( "<position([^>]*?)>(\\s+)", "$2<position$1>" );
        contenu = contenu.replaceAll( "(\\s+)</position>", "</position>$1" );
        contenu = contenu.replaceAll( "<citation([^>]*?)>(\\s+)", "$2<citation$1>" );
        contenu = contenu.replaceAll( "(\\s+)</citation>", "</citation>$1" );
        contenu = contenu.replaceAll( "<video([^>]*?)>(\\s+)", "$2<video$1>" );
        contenu = contenu.replaceAll( "(\\s+)</video>", "</video>$1" );
        contenu = contenu.replaceAll( "<secret([^>]*?)>(\\s+)", "$2<secret$1>" );
        contenu = contenu.replaceAll( "(\\s+)</secret>", "</secret>$1" );
        contenu = contenu.replaceAll( "<lien([^>]*?)>(\\s+)", "$2<lien$1>" );
        contenu = contenu.replaceAll( "(\\s+)</lien>", "</lien>$1" );
        contenu = contenu.replaceAll( "<email([^>]*?)>(\\s+)", "$2<email$1>" );
        contenu = contenu.replaceAll( "(\\s+)</email>", "</email>$1" );
        contenu = contenu.replaceAll( "<image([^>]*?)>(\\s+)", "$2<image$1>" );
        contenu = contenu.replaceAll( "(\\s+)</image>", "</image>$1" );
        contenu = contenu.replaceAll( "<acronyme([^>]*?)>(\\s+)", "$2<acronyme$1>" );
        contenu = contenu.replaceAll( "(\\s+)</acronyme>", "</acronyme>$1" );
        contenu = contenu.replaceAll( "<minicode([^>]*?)>(\\s+)", "$2<minicode$1>" );
        contenu = contenu.replaceAll( "(\\s+)</minicode>", "</minicode>$1" );

        // Rectification des doublons (fréquents...) de zCode
        contenu = contenu.replace( "<italique><italique>", "<italique>" );
        contenu = contenu.replace( "</italique></italique>", "</italique>" );
        contenu = contenu.replace( "<gras><gras>", "<gras>" );
        contenu = contenu.replace( "</gras></gras>", "</gras>" );

        return contenu;
    }
}