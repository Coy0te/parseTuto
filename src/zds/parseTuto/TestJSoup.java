package zds.parseTuto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import com.bethecoder.ascii_table.ASCIITable;

public class TestJSoup {
    private static final Pattern code               = Pattern.compile( "(<code([^>]*?)>)(.+?)(</code>)", Pattern.DOTALL );
    private static final Pattern minicode           = Pattern.compile( "(<minicode([^>]*?)>)(.+?)(</minicode>)", Pattern.DOTALL );

    private static List<String>  listeFichiersTutos = new ArrayList<String>();

    public static void main( String[] args ) throws FileNotFoundException, UnsupportedEncodingException {
        // Nom du fichier de sortie généré après la conversion
        PrintWriter writer = new PrintWriter( "data_markdowned.md", "UTF-8" );

        // Nom du fichier source à convertir
        String contenu = readFile( "data2.txt" );

        contenu = zCodeToMarkdown( contenu );
        writer.println( contenu );
        writer.close();
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

        Document document = Jsoup.parse( escapeHtmlContent( contenu ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <lien>
        // et reconstruction des liens wikipedia fr, wikipedia en et doc PHP
        for ( Element elt : document.select( "lien" ) ) {

            if ( elt.hasAttr( "doc" ) && elt.attr( "doc" ).equals( "php" ) && elt.hasAttr( "url" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + "http://php.net/"
                        + elt.attr( "url" ) + ") ", "" ) );
            } else if ( elt.hasAttr( "doc" ) && elt.attr( "doc" ).equals( "php" ) ) {
                // TODO: vérifier que elt.text() ressort bien uniquement le nom de la méthode php, et pas les éventuelles balises zCode
                // autour
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + "http://php.net/" + elt.text()
                        + ") ", "" ) );
            } else if ( elt.hasAttr( "type" ) && elt.attr( "type" ).equals( "wikipedia" ) &&
                    elt.hasAttr( "langue" ) && elt.attr( "langue" ).equals( "en" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + "http://en.wikipedia.org/wiki/"
                        + elt.attr( "url" )
                        + ") ", "" ) );
            } else if ( elt.hasAttr( "type" ) && elt.attr( "type" ).equals( "wikipedia" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + "http://fr.wikipedia.org/wiki/"
                        + elt.attr( "url" )
                        + ") ", "" ) );
            } else if ( elt.hasAttr( "url" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + elt.attr( "url" ) + ") ", "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](" + elt.html() + ") ", "" ) );
            }
        }

        // conversion <email>
        for ( Element elt : document.select( "email" ) ) {
            if ( elt.hasAttr( "nom" ) ) {
                elt.replaceWith( TextNode.createFromEncoded(
                        " [" + elt.html() + "](mailto:" + elt.attr( "nom" ) + ") ", "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( " [" + elt.html() + "](mailto:" + elt.html() + ") ", "" ) );
            }
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
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

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
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

        // conversion <math>
        for ( Element elt : document.select( "math" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "&nbsp;$" + elt.html() + "$&nbsp;", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <touche>
        for ( Element elt : document.select( "touche" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "&nbsp;||" + elt.html() + "||&nbsp;", "" ) );
        }

        // conversion <image>
        for ( Element elt : document.select( "image" ) ) {
            if ( elt.hasAttr( "legende" ) ) {
                elt.replaceWith( TextNode.createFromEncoded( " ![" + elt.attr( "legende" ) + "](" + elt.html() + ") ",
                        "" ) );
            } else {
                elt.replaceWith( TextNode.createFromEncoded( " ![](" + elt.html() + ") ", "" ) );
            }
        }

        // conversion <video>
        for ( Element elt : document.select( "video" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( " ![video](" + elt.html() + ") ", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <italique>
        for ( Element elt : document.select( "italique" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "_" + elt.html() + "_", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <gras>
        for ( Element elt : document.select( "gras" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "**" + elt.html() + "**", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <barre>
        for ( Element elt : document.select( "barre" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "&nbsp;~~" + elt.html() + "~~&nbsp;", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <souligne>
        for ( Element elt : document.select( "souligne" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <titre1>
        for ( Element elt : document.select( "titre1" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "\n# " + elt.html() + "\n", "" ) );
        }

        // conversion <titre2>
        for ( Element elt : document.select( "titre2" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "\n## " + elt.html() + "\n", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <couleur>
        for ( Element elt : document.select( "couleur" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <taille>
        for ( Element elt : document.select( "taille" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <police>
        for ( Element elt : document.select( "police" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( elt.html(), "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <liste>
        // détection des sous-listes de sous-listes
        // (tant pis pour les malades qu'ont fait des listes à plus de trois niveaux)
        for ( Element liste : document.select( "liste liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "\n        1. " + puce.html(), "" ) );
                } else {
                    puce.replaceWith( new DataNode( "\n        - " + puce.html(), "" ) );
                }
            }
            liste.replaceWith( new DataNode( liste.data(), "" ) );
        }

        // détection des sous-listes
        for ( Element liste : document.select( "liste liste" ) ) {
            for ( Element puce : liste.children() ) {
                if ( liste.hasAttr( "type" ) ) {
                    puce.replaceWith( new DataNode( "\n    1. " + puce.html(), "" ) );
                } else {
                    puce.replaceWith( new DataNode( "\n    - " + puce.html(), "" ) );
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
            liste.replaceWith( new DataNode( "\n" + liste.data() + "\n", "" ) );
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <citation>
        String[] linesTemp;
        String bufferTemp = "";
        for ( Element elt : document.select( "citation" ) ) {
            linesTemp = elt.html().split( "\r\n|\n" );
            for ( String line : linesTemp ) {
                bufferTemp += "\n> " + line;
            }
            bufferTemp += "\n";
            if ( elt.hasAttr( "nom" ) ) {
                elt.replaceWith( new DataNode( "**" + elt.attr( "nom" ) + " a écrit :**" + bufferTemp,
                        "" ) );
            } else {
                elt.replaceWith( new DataNode( bufferTemp, "" ) );
            }
            bufferTemp = "";
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
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

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <tableau>
        for ( Element tableau : document.select( "tableau" ) ) {
            String legendeTableau = "";
            if ( !tableau.getElementsByTag( "legende" ).isEmpty() ) {
                legendeTableau = tableau.getElementsByTag( "legende" ).get( 0 ).text();
                tableau.getElementsByTag( "legende" ).get( 0 ).remove();
            }
            List<String> entetes = new ArrayList<String>();
            Map<Integer, List<String>> cellules = new HashMap<Integer, List<String>>();

            int indexLigne = 0;
            for ( Element ligne : tableau.children() ) {
                if ( ligne.nodeName().equals( "ligne" ) ) {
                    List<String> cellulesLigne = new ArrayList<String>();
                    for ( Element cellule : ligne.children() ) {
                        if ( cellule.nodeName().equals( "entete" ) ) {
                            entetes.add( cellule.html() );
                        } else {
                            if ( cellule.hasAttr( "fusion_lig" ) ) {
                                // What to do? même pb pour fusion_col...
                                cellulesLigne.add( cellule.html() );
                            } else {
                                cellulesLigne.add( cellule.html() );
                            }
                        }
                    }
                    if ( !cellulesLigne.isEmpty() ) {
                        cellules.put( indexLigne, cellulesLigne );
                        indexLigne++;
                    }
                }
            }

            String[] header = entetes.toArray( new String[entetes.size()] );
            String[][] data = new String[cellules.size()][entetes.size()];
            for ( int i = 0; i < cellules.size(); i++ ) {
                data[i] = cellules.get( i ).toArray( new String[cellules.get( i ).size()] );
            }
            // DEBUG: affichage des tableaux ascii dans la console
            // TODO : les sauts de lignes cassent le tableau
            ASCIITable.getInstance().printTable( header, data );

            if ( !"".equals( legendeTableau ) ) {
                tableau.replaceWith( new DataNode( "\n" + ASCIITable.getInstance().getTable( header, data )
                        + "Table:" + legendeTableau + "\n", "" ) );
            } else {
                tableau.replaceWith( new DataNode( "\n" + ASCIITable.getInstance().getTable( header, data ) + "\n", "" ) );
            }
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
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

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <position>
        for ( Element elt : document.select( "position" ) ) {
            if ( elt.attr( "valeur" ).equals( "centre" ) ) {
                elt.replaceWith( new DataNode( "\n->" + elt.html() + "<-\n", "" ) );
            } else if ( elt.attr( "valeur" ).equals( "droite" ) ) {
                elt.replaceWith( new DataNode( "\n->" + elt.html() + "->\n", "" ) );
            } else {
                elt.replaceWith( new DataNode( elt.html(), "" ) );
            }
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <flottant>
        for ( Element elt : document.select( "flottant" ) ) {
            if ( elt.attr( "valeur" ).equals( "centre" ) ) {
                elt.replaceWith( new DataNode( "\n->" + elt.html() + "<-\n", "" ) );
            } else if ( elt.attr( "valeur" ).equals( "droite" ) ) {
                elt.replaceWith( new DataNode( "\n->" + elt.html() + "->\n", "" ) );
            } else {
                elt.replaceWith( new DataNode( elt.html(), "" ) );
            }
        }

        document = Jsoup.parse( escapeHtmlContent( document.toString() ), "", Parser.xmlParser() );
        document.outputSettings().prettyPrint( false );
        document.outputSettings().escapeMode( EscapeMode.none );
        document.outputSettings().charset( "UTF-8" );

        // conversion <code>
        for ( Element elt : document.select( "code" ) ) {
            if ( elt.hasAttr( "type" ) ) {
                elt.replaceWith( new DataNode( "\n```" + elt.attr( "type" ) + "\n" + elt.html()
                        + "\n```\n", "" ) );
            } else {
                elt.replaceWith( new DataNode( "\n```\n" + elt.html() + "\n```\n", "" ) );
            }
        }

        // conversion <minicode>
        for ( Element elt : document.select( "minicode" ) ) {
            elt.replaceWith( TextNode.createFromEncoded( "&nbsp;`" + elt.html() + "`&nbsp;", "" ) );
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
            }
        }
    }

    /*
     * Méthode d'échappement des chevrons < et > contenus au sein des sections <code> et <minicode>, pour que JSoup ne cherche pas à
     * corriger les balises HTML-like non fermées qu'elles peuvent éventuellement contenir (exemple : sans ce traitement, le code Java
     * "List<String>" deviendrait "List<string></string>" ...).
     */
    public static String escapeHtmlContent( String contenu ) {
        Matcher matcher = code.matcher( contenu );
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

        matcher = minicode.matcher( sb.toString() );
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
}