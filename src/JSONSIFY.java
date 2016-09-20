import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;



public class JSONSIFY {

	private Gson gson = null;
	private JsonReader reader = null;

	public JSONSIFY(String fileName){
		gson = new Gson();
		try{
			reader = new JsonReader(new FileReader(fileName));
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public Document read(){
		return gson.fromJson(reader, Document.class);
	}

}
