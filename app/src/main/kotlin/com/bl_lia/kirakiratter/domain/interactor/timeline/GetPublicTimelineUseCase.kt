package com.bl_lia.kirakiratter.domain.interactor.timeline

import io.reactivex.Single
import com.bl_lia.kirakiratter.domain.entity.Status
import com.bl_lia.kirakiratter.domain.executor.PostExecutionThread
import com.bl_lia.kirakiratter.domain.executor.ThreadExecutor
import com.bl_lia.kirakiratter.domain.interactor.SingleUseCase
import com.bl_lia.kirakiratter.domain.repository.TimelineRepository

class GetPublicTimelineUseCase(
        private val timelineRepository: TimelineRepository,
        private val threadExecutor: ThreadExecutor,
        private val postExecutionThread: PostExecutionThread
) : SingleUseCase<List<Status>>(threadExecutor, postExecutionThread) {

    override fun build(params: Array<out Any>): Single<List<Status>> {
        return timelineRepository.publicTimeline()
    }
}