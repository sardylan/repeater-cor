package org.thehellnet.ham.repeater.cor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepeaterCor {

    private static final Logger logger = LoggerFactory.getLogger(RepeaterCor.class);

    private State state = State.OFF;

    private boolean rxLocal = false;
    private boolean rxRemote = false;

    private boolean txLocal = false;
    private boolean txRemote = false;

    private boolean beaconTime = false;
    private boolean beaconPlaying = false;

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> state = State.DEINIT));

        state = State.INIT;

        while (state != State.DEINIT && state != State.OFF) {
            updateRxPin();
            computeLogic();
            updateTxPin();
            checkNewStatus();
            waitForEvent();
        }
    }

    private void updateRxPin() {
        logger.info("Updating RX pin status");
    }

    private void computeLogic() {
        logger.info("Computing logic");

        switch (state) {

            case OFF:
                txLocal = false;
                txRemote = false;
                break;

            case INIT:
                txLocal = false;
                txRemote = false;
                state = State.WAITING;
                break;

            case WAITING:
                if (!beaconTime && rxLocal && !rxRemote) {
                    txLocal = true;
                    txRemote = true;
                    state = State.RELAY_LOCAL;
                } else if (!beaconTime && !rxLocal && rxRemote) {
                    txLocal = true;
                    txRemote = false;
                    state = State.RELAY_REMOTE;
                } else if (beaconTime) {
                    state = State.BEACON;
                } else {
                    txLocal = false;
                    txRemote = false;
                }
                break;

            case RELAY_LOCAL:
                if (!beaconTime && !rxLocal && !rxRemote) {
                    txLocal = false;
                    txRemote = false;
                    state = State.WAITING;
                }
                break;

            case RELAY_REMOTE:
                break;

            case BEACON:
                if (!beaconTime) {
                    state = State.WAITING;
                } else if (!beaconPlaying && !rxLocal && !rxRemote) {
                    beaconPlaying = true;
                    startBeaconPlayer();
                }
                break;

            case DEINIT:
                txLocal = false;
                txRemote = false;
                state = State.OFF;
                break;

        }
    }

    private void updateTxPin() {
        logger.info("Update TX pin status");
    }

    private void checkNewStatus() {
        logger.info("Doing actions for new state");
    }

    private void waitForEvent() {
        logger.info("Waiting for event");
    }

    private void startBeaconPlayer() {

    }
}
