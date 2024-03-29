(ns de.explorama.backend.projects.queue-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.projects.queue :refer [queue]]))

(def test-queue1 (conj (queue) :a :b :c :d :e :f :g))

(def test-queue2 (queue :a :b :c :d :e :f :g))

(def test-queue3 (atom (queue :a :b :c :d :e :f :g)))

(def test-queue4 (atom (queue)))
(swap! test-queue4 conj :a :b :c :d :e :f :g)

(deftest queue-test1
  (testing "queue-test1"
    (is (= :a (peek test-queue1)))
    (is (= '(:b :c :d :e :f :g) (seq (pop test-queue1))))
    (is (seq test-queue1))))

(deftest queue-test2
  (testing "queue-test2"
    (is (= :a (peek test-queue2)))
    (is (= '(:b :c :d :e :f :g) (seq (pop test-queue2))))
    (is (seq test-queue2))))

(deftest queue-test3
  (testing "queue-test3"
    (is (= :a (peek @test-queue3)))
    (is (= '(:b :c :d :e :f :g) (seq (pop @test-queue3))))
    (is (seq @test-queue3))))

(deftest queue-test4
  (testing "queue-test4"
    (is (= :a (peek @test-queue4)))
    (is (= '(:b :c :d :e :f :g) (seq (pop @test-queue4))))
    (is (seq @test-queue4))))
