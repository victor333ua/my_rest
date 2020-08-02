package com.jasn.my_rest.utils;

import com.jasn.my_rest.exception.GifNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Optional;

@Slf4j
public class Vocabulary {
    private JSONObject users = new JSONObject();

    public void putGif(String userId, String theme, String pathToGif) {
        if(!users.has(userId)) users.put(userId, new JSONObject());

        JSONObject userObject = users.getJSONObject(userId);
        JSONArray array = userObject.optJSONArray(theme);
        if(array==null) {
            userObject.append(theme, pathToGif);
            return;
        }
// add path gif to memory if no exist
        Iterator iter = array.iterator();
        while(iter.hasNext())
            if( ((String)iter.next()).equals(pathToGif)) return;
         array.put(pathToGif);
    }

    public Optional<String> getGif(String idUser, String theme) {
        if(!users.has(idUser)) return Optional.empty();
        JSONArray listGifs = ((JSONObject)users.get(idUser)).optJSONArray(theme);
        if(listGifs == null) return Optional.empty();
        int count = listGifs.length();
        return Optional.of(listGifs.getString((int)Math.random()*count));
    }

    public void deleteVocabulary(String idUser, String theme) {
        if(!users.has(idUser)) return;
        if(theme.isEmpty()) {
            users.remove(idUser);
            return;
        }
        users.getJSONObject(idUser).remove(theme);
    }
}
