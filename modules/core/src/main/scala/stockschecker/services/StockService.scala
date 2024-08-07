package stockschecker.services

import cats.MonadThrow
import cats.syntax.flatMap.*
import stockschecker.clients.MarketDataClient
import stockschecker.domain.{Stock, Ticker}
import stockschecker.repositories.StockRepository

trait StockService[F[_]]:
  def seedStocks: F[Unit]
  def get(ticker: Ticker, fetchLatest: Boolean = false): F[Stock]

final private class LiveStockService[F[_]](
    private val repository: StockRepository[F],
    private val marketDataClient: MarketDataClient[F]
)(using
    F: MonadThrow[F]
) extends StockService[F] {
  override def seedStocks: F[Unit] =
    marketDataClient.getAllTradedStocks.flatMap(repository.save)

  override def get(ticker: Ticker, fetchLatest: Boolean = false): F[Stock] = ???
}

object StockService:
  def make[F[_]](repo: StockRepository[F], client: MarketDataClient[F])(using F: MonadThrow[F]): F[StockService[F]] =
    F.pure(LiveStockService[F](repo, client))
