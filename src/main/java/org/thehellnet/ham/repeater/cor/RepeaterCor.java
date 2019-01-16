package org.thehellnet.ham.repeater.cor;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class RepeaterCor {

    private static final Logger logger = LoggerFactory.getLogger(RepeaterCor.class);

    private GpioController gpioController;

    private GpioPinDigitalInput rxLocalPin;
    private GpioPinDigitalInput rxRemotePin;
    private GpioPinDigitalOutput txLocalPin;
    private GpioPinDigitalOutput txRemotePin;

    private State state = State.OFF;
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private Thread beaconThread;

    private boolean rxLocal = false;
    private boolean rxRemote = false;
    private boolean beaconTime = false;

    private boolean txLocal = false;
    private boolean txRemote = false;

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

        pinSetup();
        addPinListeners();

        beaconThread = new Thread(() -> {
            while (!beaconThread.isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    break;
                }

                DateTime dateTime = DateTime.now();

                if (dateTime.getSecondOfMinute() != 0) {
                    continue;
                }
                if (dateTime.getMinuteOfHour() % 10 != 0) {
                    continue;
                }

                beaconTime = true;

                try {
                    eventQueue.put(Event.BEACON_TIME);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
        });
        beaconThread.start();

        mainLoop();

        gpioController.shutdown();
    }

    private void pinSetup() {
        gpioController = GpioFactory.getInstance();

        rxLocalPin = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_00);
        rxRemotePin = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_01);

        txLocalPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW);
        txRemotePin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03, PinState.LOW);

        txLocalPin.setShutdownOptions(true, PinState.LOW);
        txRemotePin.setShutdownOptions(true, PinState.LOW);
    }

    private void addPinListeners() {
        rxLocalPin.addListener((GpioPinListenerDigital) event -> {
            try {
                eventQueue.put(event.getState().isHigh() ? Event.RX_LOCAL_OPEN : Event.RX_LOCAL_CLOSE);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        });

        rxRemotePin.addListener((GpioPinListenerDigital) event -> {
            try {
                eventQueue.put(event.getState().isHigh() ? Event.RX_REMOTE_OPEN : Event.RX_REMOTE_CLOSE);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        });
    }

    private void mainLoop() {
        state = State.INIT;

        while (state != State.DEINIT && state != State.OFF) {
            updateRxPin();
            computeLogic();
            parseStatus();
            updateTxPin();

            if (state == State.BEACON) {
                beaconPlay();
            }

            waitForEvent();
        }
    }

    private void updateRxPin() {
        logger.info("Updating RX pin status");

        rxLocal = rxLocalPin.getState().isHigh();
        rxRemote = rxRemotePin.getState().isHigh();
    }

    private void computeLogic() {
        logger.info("Computing logic");

        switch (state) {

            case OFF:
                break;

            case INIT:
                state = State.WAITING;
                break;

            case WAITING:
                if (beaconTime) {
                    state = State.BEACON;
                } else {
                    if (rxLocal) {
                        state = State.RELAY_LOCAL;
                    } else if (rxRemote) {
                        state = State.RELAY_REMOTE;
                    }
                }
                break;

            case RELAY_LOCAL:
                if (!rxLocal) {
                    state = State.WAITING;
                }
                checkBeaconTime();
                break;

            case RELAY_REMOTE:
                if (!rxRemote) {
                    state = State.WAITING;
                }
                checkBeaconTime();
                break;

            case BEACON:
                if (!beaconTime) {
                    state = State.WAITING;
                }
                break;

            case DEINIT:
                state = State.OFF;
                break;

        }
    }

    private void parseStatus() {
        logger.info("Parse status");

        switch (state) {

            case OFF:
                txLocal = false;
                txRemote = false;
                break;

            case INIT:
                txLocal = false;
                txRemote = false;
                break;

            case WAITING:
                txLocal = false;
                txRemote = false;
                break;

            case RELAY_LOCAL:
                txLocal = true;
                txRemote = true;
                break;

            case RELAY_REMOTE:
                txLocal = true;
                txRemote = false;
                break;

            case BEACON:
                txLocal = true;
                txRemote = false;
                break;

            case DEINIT:
                txLocal = false;
                txRemote = false;
                break;
        }
    }

    private void updateTxPin() {
        logger.info("Update TX pin status");

        txLocalPin.setState(txLocal);
        txRemotePin.setState(txRemote);
    }

    private void beaconPlay() {
        logger.info("Starting beacon");

        new Thread(() -> {
            try {
                Runtime.getRuntime().exec("mplayer beacon.wav");
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void waitForEvent() {
        logger.info("Waiting for event");

        Event event;

        try {
            event = eventQueue.take();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return;
        }

        logger.debug(event.name());
    }

    private void checkBeaconTime() {
        if (!rxLocal && !rxRemote) {
            state = State.BEACON;
        }
    }
}
