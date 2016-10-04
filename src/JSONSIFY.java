import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;



public class JSONSIFY {

	private Gson 		mGson 	= null;
	private JsonReader 	mReader = null;

	/**
	 * Read json file
	 * @param fileName
	 */
	public JSONSIFY(String fileName){
		mGson = new Gson();
		try{
			mReader = new JsonReader(new FileReader(fileName));
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Parse json file into object
	 * @return Article class with parsed json content
	 */
	public Article read(){
		return mGson.fromJson(mReader, Article.class);
	}

}
