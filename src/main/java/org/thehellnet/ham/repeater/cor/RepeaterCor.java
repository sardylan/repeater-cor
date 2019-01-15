package org.thehellnet.ham.repeater.cor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepeaterCor {

    private static final Logger logger = LoggerFactory.getLogger(RepeaterCor.class);

    public static void main(String[] args) {
        RepeaterCor repeaterCor = new RepeaterCor();

        logger.info("START");

        try {
            repeaterCor.run(args);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        logger.info("END");
    }

    private void run(String[] args) throws Exception {

    }
}
