/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boxalino.client.SDK;

import Exception.BoxalinoException;

/**
 *
 * @author HASHIR
 */
public class BxSearchRequest extends BxRequest {

    public BxSearchRequest(String language, String queryText, int max, String choiceId) throws BoxalinoException {
        super(language, choiceId.isEmpty() ? "search" : choiceId, max, 0);

        super.setQuerytext(queryText);

    }

}
