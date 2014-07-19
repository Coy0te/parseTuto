package zds.parseTuto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerFactoryConfigurationError;

public class TestBatchRename {
    private static List<String> listeCheminsFichiersTutos = new ArrayList<String>();
    private static final String FOLDER_TUTO_ENTER         = "/Users/gaowenjia/work/tutos_ths/tutos_sdzv3/Sources/";

    public static void main( String[] args ) throws TransformerFactoryConfigurationError {
        File folder = new File( FOLDER_TUTO_ENTER );
        listFilesForFolder( folder );
        for ( String fileStr : listeCheminsFichiersTutos ) {
            System.out.println( "#### " + fileStr + " ####" );

            try {
                delete( new File( fileStr ) );
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }
    }

    /*
     * Méthode helper pour le listage des fichiers d'un répertoire.
     */
    public static void listFilesForFolder( final File folder ) {
        for ( final File fileEntry : folder.listFiles() ) {
            if ( fileEntry.isDirectory() ) {
                if ( "images".equals( fileEntry.getName() ) ) {
                    listeCheminsFichiersTutos.add( fileEntry.getAbsolutePath() );
                } else {
                    listFilesForFolder( fileEntry );
                }
            }
        }
    }

    static public void delete( File f ) throws IOException {
        if ( f.isDirectory() ) {
            for ( File c : f.listFiles() )
                delete( c );
        }
        if ( !f.delete() )
            throw new FileNotFoundException( "Failed to delete file: " + f );
    }

    static public void zipFolder( String srcFolder, String destZipFile ) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream( destZipFile );
        zip = new ZipOutputStream( fileWriter );

        addFolderToZip( "", srcFolder, zip );
        zip.flush();
        zip.close();
    }

    static private void addFileToZip( String path, String srcFile, ZipOutputStream zip )
            throws Exception {

        File folder = new File( srcFile );
        if ( folder.isDirectory() ) {
            addFolderToZip( path, srcFile, zip );
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream( srcFile );
            zip.putNextEntry( new ZipEntry( path + "/" + folder.getName() ) );
            while ( ( len = in.read( buf ) ) > 0 ) {
                zip.write( buf, 0, len );
            }
        }
    }

    static private void addFolderToZip( String path, String srcFolder, ZipOutputStream zip )
            throws Exception {
        File folder = new File( srcFolder );

        for ( String fileName : folder.list() ) {
            if ( path.equals( "" ) ) {
                addFileToZip( folder.getName(), srcFolder + "/" + fileName, zip );
            } else {
                addFileToZip( path + "/" + folder.getName(), srcFolder + "/" + fileName, zip );
            }
        }
    }

}