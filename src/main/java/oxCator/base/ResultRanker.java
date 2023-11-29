package oxCator.base;

import java.util.*;

public class ResultRanker {
    private HashMap<String, PositionalIndex> positionalIndex; //->df & M
    private HashMap<String, Integer> termCount; //->Tf
    public HashMap<Integer, String> documentList; //-> N
}
