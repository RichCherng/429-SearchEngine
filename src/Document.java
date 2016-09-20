import java.util.ArrayList;


public class Document{

		private ArrayList<Article> documents;

		public void print(){
			System.out.println(documents.size());
			for( Article a: documents){
				System.out.println(a.title);
			}
		}

		public ArrayList<Article> getDocument(){
			return documents;
		}

		public class Article{
			String body;
			String url;
			String title;
		}
}