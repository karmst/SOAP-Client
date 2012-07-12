package com.belkast.soap;

import com.beust.jcommander.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class Parser {

    @Parameter(description = "parameters")
    public List<String> parameters = new ArrayList<String>();
    
    @Parameter(names = "--props" , description = "Location of the Properties file")
    public String varPropertiesFile;
    
    @Parameter(names = "--key" , description = "Key used for encryption", required = true)
    public String varKey;

    @Parameter(names = "--encrypt", description = "Encrypt this value using the Key")
    public String varEncrypt;
    
    @Parameter(names = "--replace", description = "Tokens for which to search", variableArity = true, splitter = SpaceParameterSplitter.class)
    public List<String> varTokens = new ArrayList<String>();

    @Parameter(names = "--help", description = "print this message", help = true)
    public boolean help;

    @Parameter(names = "--debug", description = "Used to debug the program")
    public String debug;
}
