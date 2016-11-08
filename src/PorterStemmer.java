
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PorterStemmer {

   // a single consonant
   private static final String c = "[^aeiou]";
   // a single vowel
   private static final String v = "[aeiouy]";

   // a sequence of consonants; the second/third/etc consonant cannot be 'y'
   private static final String C = c + "[^aeiouy]*";
   // a sequence of vowels; the second/third/etc cannot be 'y'
   private static final String V = v + "[aeiou]*";

   // this regex pattern tests if the token has measure > 0 [at least one VC].
   private static final Pattern mGr0 = Pattern.compile("^(" + C + ")?" + V + C);

   // (*v*)
   private static final Pattern p = Pattern.compile(V);

   // *d:  stem ends with a double consonant
   private static final Pattern d = Pattern.compile(c + c + "$");

   // *o: the stem ends cvc, where the second c is not W, X, or Y (e.g. -WIL, -HOP).
   private static final Pattern o = Pattern.compile(C + v + "[^aeiouwxy]"); // ? $

   // Two or more (VC)
   private static final Pattern two = Pattern.compile("^(" + C + ")?" + "("+ V + C + "){2}" );

   // HashMap for replacement in step 2
   private static final Map<String, String> step2Pair;
   static {
       HashMap<String, String> aMap = new HashMap<String, String>();
       aMap.put("ational"	, "ate" );
       aMap.put("tional"	, "tion");
       aMap.put("enci"		, "ence");
       aMap.put("anci"		, "ance");
       aMap.put("izer"		, "ize" );
       aMap.put("abli"		, "able");
       aMap.put("alli"		, "al"	);
       aMap.put("entli"		, "ent"	);
       aMap.put("eli"		, "e"	);
       aMap.put("ousli"		, "ous"	);
       aMap.put("ization"	, "ize"	);
       aMap.put("ation"		, "ate"	);
       aMap.put("ator"		, "ate"	);
       aMap.put("alism"		, "al"	);
       aMap.put("iveness"	, "ive"	);
       aMap.put("fulness"	, "ful"	);
       aMap.put("ousness"	, "ous"	);
       aMap.put("aliti"		, "al"	);
       aMap.put("iviti"		, "ive"	);
       aMap.put("biliti"	, "ble"	);
       step2Pair =  Collections.unmodifiableMap(aMap);
   }

   private static final Map<String, String> step3Pair;
   static {
	   HashMap<String, String> aMap = new HashMap<String, String>();
	   aMap.put("icate"	, "ic" );
	   aMap.put("ative"	, ""   );
	   aMap.put("alize"	, "al" );
	   aMap.put("iciti"	, "ic" );
	   aMap.put("ical"	, "ic" );
	   aMap.put("ful"	, ""   );
	   aMap.put("ness"	, ""   );
	   step3Pair = Collections.unmodifiableMap(aMap);
   }

   private static final Map<String, String> step4Pair;
   private static final String[] step4Keys = {"al"	, "ance", "ence", "er" , "ic" , "able", "ible", "ant", "ement",
		   									  "ment", "ent" , "ou"  , "ism", "ate", "iti" , "ous" , "ive", "ize" };
   static {
	   HashMap<String, String> aMap = new HashMap<String, String>();
	   aMap.put("al" 	, "" );
	   aMap.put("ance"	, "" );
	   aMap.put("ence"	, "" );
	   aMap.put("er"	, "" );
	   aMap.put("ic"	, "" );
	   aMap.put("able"	, "" );
	   aMap.put("ible"	, "" );
	   aMap.put("ant"	, "" );
	   aMap.put("ement"	, "" );
	   aMap.put("ment"	, "" );
	   aMap.put("ent"	, "" );
	   aMap.put("ou"	, "" );
	   aMap.put("ism"	, "" );
	   aMap.put("ate"	, "" );
	   aMap.put("iti"	, "" );
	   aMap.put("ous"	, "" );
	   aMap.put("ive"	, "" );
	   aMap.put("ize"	, "" );
	   step4Pair = Collections.unmodifiableMap(aMap);
   }

   // add more Pattern variables for the following patterns:
   // m equals 1: token has measure == 1
   // m greater than 1: token has measure > 1
   // vowel: token has a vowel after the first (optional) C
   // double consonant: token ends in two consonants that are the same,
   //			unless they are L, S, or Z. (look up "backreferencing" to help
   //			with this)
   // m equals 1, cvc: token is in Cvc form, where the last c is not w, x,
   //			or y.

   public static String processToken(String token) {

	   Pattern ssEnd = Pattern.compile("^[\\w]+[^s]s$");
	   Matcher m = ssEnd.matcher(token);

      if (token.length() < 3) {
         return token; // token must be at least 3 chars
      }
      // step 1a
      if (token.endsWith("sses")) {

         token = token.substring(0, token.length() - 2);

      } else if (token.endsWith("ies")){

    	 token = token.substring(0,token.length()-2);

      } else if (m.find()){

    	 token = token.substring(0, token.length()-1);
      }

      // program the other steps in 1a.
      // note that Step 1a.3 implies that there is only a single 's' as the
      //	suffix; ss does not count. you may need a regex pattern here for
      // "not s followed by s".

      // step 1b
      boolean doStep1bb = false;
      //		step 1b
      if (token.endsWith("eed")) { // 1b.1
         // token.substring(0, token.length() - 3) is the stem prior to "eed".
         // if that has m>0, then remove the "d".
         String stem = token.substring(0, token.length() - 3);
         if (mGr0.matcher(stem).find()) { // if the pattern matches the stem
            token = stem + "ee";
         }
      } else if (token.endsWith("ed")){
    	  String stem = token.substring(0, token.length() - 2);
    	  if( p.matcher(stem).find()){
    		  token = stem;
    		  doStep1bb = true;
    	  }

      } else if (token.endsWith("ing")){
    	  String stem = token.substring(0, token.length() - 3);
    	  if (p.matcher(stem).find()){
    		  token = stem;
    		  doStep1bb = true;
    	  }
      }

      // program the rest of 1b. set the boolean doStep1bb to true if Step 1b*
      // should be performed.

      // step 1b*, only if the 1b.2 or 1b.3 were performed.
      if (doStep1bb) {

         if (token.endsWith("at") || token.endsWith("bl")
          || token.endsWith("iz")) {

            token = token + "e";
         } else if ( d.matcher(token).find()
        		 && !(token.endsWith("l") || token.endsWith("s")|| token.endsWith("z")) ){
        	 token = token.substring(0, token.length() - 1);
         } else if ((!two.matcher(token).find()
        		 && mGr0.matcher(token).find())
        		 && o.matcher(token).find()){ // (m < 2 && m > 0) = m = 1
        	 token += "e";
         }
         // use the regex patterns you wrote for 1b*.4 and 1b*.5
      }



      // step 1c
      // program this step. test the suffix of 'y' first, then test the
      // condition *v* on the stem.

      if (token.endsWith("y")){

    	  String stem = token.substring(0, token.length() - 1);
    	  if (p.matcher(stem).find()){
    		  token = stem + 'i';
    	  }
      }


      // step 2
      // program this step. for each suffix, see if the token ends in the
      // suffix.
      //    * if it does, extract the stem, and do NOT test any other suffix.
      //    * take the stem and make sure it has m > 0.
      //        * if it does, complete the step and do not test any others.
      //          if it does not, attempt the next suffix.

      // you may want to write a helper method for this. a matrix of
      // "suffix"/"replacement" pairs might be helpful. It could look like
      // string[][] step2pairs = {  new string[] {"ational", "ate"},
      //										new string[] {"tional", "tion"}, ....

      for(Entry<String, String> entry : step2Pair.entrySet()) {
    	    String key = entry.getKey();
    	    if(token.endsWith(key)){
    	    	String stem = token.substring(0, token.length() - key.length());
    	    	if (mGr0.matcher(stem).find()){
    	    		token = stem + entry.getValue();
    	    	}
    	    	break;
    	    }
      }


      // step 3
      // program this step. the rules are identical to step 2 and you can use
      // the same helper method. you may also want a matrix here.

      for(Entry<String, String> entry : step3Pair.entrySet()){
    	  String key = entry.getKey();
  	      if(token.endsWith(key)){
  	    	  String stem = token.substring(0, token.length() - key.length());
  	    	  if (mGr0.matcher(stem).find()){
  	    		  token = stem + entry.getValue();
  	    	}
  	    	break;
  	    }
      }


      // step 4
      // program this step similar to step 2/3, except now the stem must have
      // measure > 1.
      // note that ION should only be removed if the suffix is SION or TION,
      // which would leave the S or T.
      // as before, if one suffix matches, do not try any others even if the
      // stem does not have measure > 1.


      for(String key : step4Keys){
    	  if(token.endsWith(key)){
    		  String stem = token.substring(0, token.length() - key.length());
    		  if(two.matcher(stem).find()){ // m > 1
    			  token = stem + step4Pair.get(key);
    		  }
    		  break;
    	  }
      }

      if(token.endsWith("sion") || token.endsWith("tion")){
    	  String stem = token.substring(0, token.length() - "ion".length());
    	  if(two.matcher(stem).find()){ // m >= 2 or m > 1
    		  token = stem;
    	  }
      }

      // step 5
      // program this step. you have a regex for m=1 and for "Cvc", which
      // you can use to see if m=1 and NOT Cvc.
      // all your code should change the variable token, which represents
      // the stemmed term for the token.
      if (token.endsWith("e")){
    	  String stem = token.substring(0, token.length() - 1);
    	  if(two.matcher(stem).find()){
    		  token = stem;
    	  } else if (mGr0.matcher(stem).find() && !two.matcher(stem).find() && !o.matcher(token).find() ){ //m > 0 && m < 2 && not *o
    		  token = stem;
    	  }
      }

      if (two.matcher(token).find() && d.matcher(token).find() && token.endsWith("l")){
    	  token = token.substring(0, token.length() - 1);
      }


      return token;
   }

}
