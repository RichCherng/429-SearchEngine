import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryParser {

	public DirectoryParser(){

	}

	/**
	 * Go through all the documents in the directory and parse it.
	 * @param directory
	 * @return true-if successful, otherwise false.
	 */
	public boolean parseDirectory(final Path directory){

		if(!Files.exists(directory)){
			System.out.println("Directory doesn't exists");
			return false;
		}


		try{
			System.out.println(directory.toString());

			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

	            public FileVisitResult preVisitDirectory(Path dir,
	             BasicFileAttributes attrs) {
	               // make sure we only process the current working directory
	               if (directory.equals(dir)) {
	                  return FileVisitResult.CONTINUE;
	               }
	               return FileVisitResult.SKIP_SUBTREE;
	            }

	            public FileVisitResult visitFile(Path file,
	             BasicFileAttributes attrs) {
	               // only process .txt files
	               if (file.toString().endsWith(".json")) {

//	                  buildDictionary(file, dictionary);
//	                  files.add(file.getFileName().toString());
	            	   System.out.println(file.toString());

	               }
	               return FileVisitResult.CONTINUE;
	            }

	            // don't throw exceptions if files are locked/other errors occur
	            public FileVisitResult visitFileFailed(Path file,
	             IOException e) {

	               return FileVisitResult.CONTINUE;
	            }
	         });
		} catch (Exception ex){
			return false;
		}


		return true;
	}
}
