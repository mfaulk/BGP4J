/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 * File: org.bgp4j.netty.fsm.FireEventTimeManager.java 
 */
package org.bgp4j.netty.fsm;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.listeners.TriggerListenerSupport;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
class FireEventTimeManager<T extends FireEventTimeJob> {

	private class ClearTriggerData extends TriggerListenerSupport {
		private String name = UUID.randomUUID().toString();
		
		@Override
		public String getName() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.quartz.listeners.TriggerListenerSupport#triggerComplete(org.quartz.Trigger, org.quartz.JobExecutionContext, org.quartz.Trigger.CompletedExecutionInstruction)
		 */
		@Override
		public void triggerComplete(Trigger trigger,
				JobExecutionContext context,
				CompletedExecutionInstruction triggerInstructionCode) {
			firedWhen = null;
			triggerKey = null;
		}
	}
	
	private ClearTriggerData clearTriggerData = new ClearTriggerData();
	
	private @Inject Scheduler scheduler;
	private JobDetail jobDetail;
	private TriggerKey triggerKey;
	private Date firedWhen;
	
	void createJobDetail(Class<T> jobClass, InternalFSM fsm, Map<String, Object> additionalJobData) throws SchedulerException {
		JobDataMap map = new JobDataMap();
		
		map.put(FireEventTimeJob.FSM_KEY, this);

		if(additionalJobData != null)  {
			for(Entry<String, Object> entry : additionalJobData.entrySet())
				map.put(entry.getKey(), entry.getValue());
		}
		
		jobDetail = JobBuilder.newJob(jobClass).usingJobData(map).build();
		
		scheduler.getListenerManager().addTriggerListener(clearTriggerData, EverythingMatcher.allTriggers());
	}
	
	void createJobDetail(Class<T> jobClass, InternalFSM fsm) throws SchedulerException {
		createJobDetail(jobClass, fsm, null);
	}
	
	void shutdown() throws SchedulerException {
		cancelJob();
		
		scheduler.getListenerManager().removeTriggerListener(clearTriggerData.getName());
	};
	
	void scheduleJob(int whenInSeconds) throws SchedulerException {
		triggerKey = TriggerKey.triggerKey(UUID.randomUUID().toString());
		
		firedWhen = scheduler.scheduleJob(jobDetail, TriggerBuilder.newTrigger()
		.withIdentity(triggerKey)
		.withSchedule(SimpleScheduleBuilder.simpleSchedule())
		.startAt(new Date(System.currentTimeMillis() + whenInSeconds*1000L))
		.build());
	}
	
	boolean isJobScheduled() throws SchedulerException {
		if(triggerKey == null)
			return false;
		return scheduler.checkExists(triggerKey);
	}

	void cancelJob() throws SchedulerException {
		if(triggerKey != null) {
			scheduler.unscheduleJob(triggerKey);
			triggerKey = null;
			firedWhen = null;
		}
	}
	
	/**
	 * @return the firedWhen
	 */
	Date getFiredWhen() {
		return firedWhen;
	}
}
