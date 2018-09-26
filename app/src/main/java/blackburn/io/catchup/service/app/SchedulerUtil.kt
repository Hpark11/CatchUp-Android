package blackburn.io.catchup.service.app

import io.reactivex.*

class SchedulerUtil(val background: Scheduler, val foreground: Scheduler) {
  fun <T> forObservable(): (Observable<T>) -> Observable<T> {
    return { observable: Observable<T> ->
      observable.subscribeOn(background).observeOn(foreground)
    }
  }

  fun <T> forSingle(): (Single<T>) -> Single<T> {
    return { single: Single<T> ->
      single.subscribeOn(background).observeOn(foreground)
    }
  }

  fun <T> forMaybe(): (Maybe<T>) -> Maybe<T> {
    return { maybe: Maybe<T> ->
      maybe.subscribeOn(background).observeOn(foreground)
    }
  }

  fun forCompletable(): (Completable) -> Completable {
    return { completable: Completable ->
      completable.subscribeOn(background).observeOn(foreground)
    }
  }

  fun <T> forFlowable(): (Flowable<T>) -> Flowable<T> {
    return { flowable: Flowable<T> ->
      flowable.subscribeOn(background).observeOn(foreground)
    }
  }
}