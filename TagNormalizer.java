package com.swivel.recipes;

// this prevents us from having issues like "egg" and "eggs" from being created
// and other common cases. tags are put under the "one true" name
public final class TagNormalizer {
	public static String normalize(String name) {
	    if (name == null) return null;
	    String s = name.trim().toLowerCase().replaceAll("\\s+", " ");
	    // plurals & common endings
	    if (s.endsWith("ies") && s.length() > 3) s = s.substring(0, s.length()-3) + "y"; // berries to berry
	    else if (s.endsWith("ses") || s.endsWith("xes") || s.endsWith("zes")) s = s.substring(0, s.length()-2); // boxes to box
	    else if (s.endsWith("s") && !s.endsWith("ss")) s = s.substring(0, s.length()-1); // eggs to egg
	    // fix common exceptions
	    if (s.equals("tomatoe")) s = "tomato"; 
	    return s;
	}
}
