package com.omc.preprocess.service.processor;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.omc.geode.service.api.OmcAlertService;
import com.omc.geode.service.domain.OmcGeodeServiceResult;
import com.omc.service.domain.OmcAlertDetail;
import com.omc.service.domain.OmcAlertOrigin;
import com.omc.service.domain.OmcEvent;
import com.omc.service.domain.OmcObserverState;
import com.omc.service.domain.OmcObserver.ResponseState;
import com.omc.service.domain.OmcTask;
import com.omc.service.util.OmcEventUtil;

public class OmcPreprocessServiceDeliveryTask extends OmcTask {

	private final Log logger = LogFactory.getLog(OmcPreprocessServiceDeliveryTask.class);
	private OmcObserverState omcObserverState;
	private int deliveryRetryCount;
	private OmcAlertService omcAlertService;

	public OmcPreprocessServiceDeliveryTask(BlockingQueue<OmcEvent> deliveryQueue, 
			OmcObserverState omcObserverState,
			int deliveryRetryCount,
			OmcAlertService omcAlertService) {
		super(deliveryQueue);
		this.omcObserverState = omcObserverState;
		this.deliveryRetryCount = deliveryRetryCount;
		this.omcAlertService = omcAlertService;
	}

	@Override
	public void run() {
		logger.debug("Starting Delivery Queue worker thread");
		while (true) {
			try {
				OmcEvent omcEvent = this.omcQueue.take();
				logger.debug("Get task from Delivery Queue: " + omcEvent.toString());
				if (process(omcEvent)) {
					omcObserverState.incrementSuccCount();
					OmcEventUtil.updateObserverDeliveryState(omcEvent, ResponseState.SUCCESS);
					logger.debug("Successfully deliveried event: " + omcEvent.toString());
				} else {
					logger.debug("Exceeded max retry count, delivery service error!");
					break;
				}
			} catch (InterruptedException e) {
				logger.error("Thread was interrupted!");
				break;
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		logger.debug("Delivery Queue worker thread stopped!");
	}

	private boolean process(OmcEvent omcEvent) {
		int retryCount = 0;
		while(retryCount <= deliveryRetryCount) {
			try {
				Thread.sleep(5000);
				OmcGeodeServiceResult detailResult = omcAlertService.createAlertDetail(omcEvent.getEventid(), new OmcAlertDetail(omcEvent));
				OmcGeodeServiceResult originResult = omcAlertService.createAlertOrigin(omcEvent.getEventid(), new OmcAlertOrigin(omcEvent));
				if(!detailResult.isSuccessful()) {
					throw new Exception("Error creating alert_detail record: [" + detailResult.getErrorCode() + "] " + detailResult.getErrorMessage());
				}
				if(!originResult.isSuccessful()) {
					throw new Exception("Error creating alert_origin record: [" + originResult.getErrorCode() + "] " + originResult.getErrorMessage());
				}
				return true;
			} catch (Exception e) {
				omcObserverState.incrementFailCount();
				logger.error(e.getMessage() + " [Retry " + retryCount++ + "/" + deliveryRetryCount + "]");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					logger.error("Thread was interrupted!");
				}
			}
		}
		return false;
	}
}