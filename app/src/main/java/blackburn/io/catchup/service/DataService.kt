package blackburn.io.catchup.service

import blackburn.io.catchup.app.Define
import blackburn.io.catchup.di.scope.AppScope
import com.amazonaws.amplify.generated.graphql.CheckAppVersionQuery
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.Subscription
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ResponseFetcher
import com.apollographql.apollo.internal.util.Cancelable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.jetbrains.annotations.NotNull
import javax.inject.Inject
import io.reactivex.ObservableEmitter
import io.reactivex.FlowableEmitter
import io.reactivex.Flowable
import io.reactivex.BackpressureStrategy

@AppScope
class DataService @Inject constructor(private val client: AWSAppSyncClient) {

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    @NotNull query: Query<D, T, V>,
    fetcher: ResponseFetcher = AppSyncResponseFetchers.CACHE_AND_NETWORK
  ): Observable<Response<T>> {

    return Observable.create { emitter ->
      val call = client.query(query).responseFetcher(fetcher)
      cancelOnDisposed(emitter, call)

      call.enqueue(object : GraphQLCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
          if (!emitter.isDisposed) emitter.onNext(response)
        }

        override fun onFailure(e: ApolloException) {
          if (!emitter.isDisposed) emitter.onError(e)
        }

        override fun onStatusEvent(event: GraphQLCall.StatusEvent) {
          if (event == GraphQLCall.StatusEvent.COMPLETED && !emitter.isDisposed) {
            emitter.onComplete()
          }
        }
      })
    }
  }

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    @NotNull subscription: Subscription<D, T, V>
  ): Flowable<Response<T>> {
    return from(subscription, BackpressureStrategy.LATEST)
  }

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    @NotNull subscription: Subscription<D, T, V>,
    backPressureStrategy: BackpressureStrategy
  ): Flowable<Response<T>> {

    return Flowable.create({ emitter ->
      val call = client.subscribe(subscription)
      cancelOnDisposed(emitter, call)

      call.execute(object : AppSyncSubscriptionCall.Callback<T> {
        override fun onResponse(response: Response<T>) {
          if (!emitter.isCancelled) emitter.onNext(response)
        }

        override fun onFailure(e: ApolloException) {
          if (!emitter.isCancelled) emitter.onError(e)
        }

        override fun onCompleted() {
          if (!emitter.isCancelled) emitter.onComplete()
        }
      })
    }, backPressureStrategy)
  }

  private fun <T> cancelOnDisposed(emitter: FlowableEmitter<T>, cancelable: Cancelable) {
    emitter.setDisposable(disposable(cancelable))
  }

  private fun <T> cancelOnDisposed(emitter: ObservableEmitter<T>, cancelable: Cancelable) {
    emitter.setDisposable(disposable(cancelable))
  }

  private fun disposable(cancelable: Cancelable): Disposable {
    return object : Disposable {
      override fun dispose() { cancelable.cancel() }
      override fun isDisposed() = cancelable.isCanceled
    }
  }

  fun requestAppVersion(): Observable<Response<CheckAppVersionQuery.Data>> {
    return from(CheckAppVersionQuery.builder().platform(Define.PLATFORM_ANDROID).build(), AppSyncResponseFetchers.NETWORK_ONLY)
  }
}

