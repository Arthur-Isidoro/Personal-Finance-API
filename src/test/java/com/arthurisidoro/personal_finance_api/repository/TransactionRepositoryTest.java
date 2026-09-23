package com.arthurisidoro.personal_finance_api.repository;

import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.entity.Transaction;
import com.arthurisidoro.personal_finance_api.entity.TransactionType;
import com.arthurisidoro.personal_finance_api.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void deveFiltrarTransacoesPorTipoECategoria() {
        User user = criarUsuario("arthur@teste.com");
        Category alimentacao = criarCategoria("Alimentação", user);
        Category transporte = criarCategoria("Transporte", user);

        criarTransacao("Almoço", new BigDecimal("30.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 10), alimentacao, user);
        criarTransacao("Uber", new BigDecimal("15.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 11), transporte, user);
        criarTransacao("Salário", new BigDecimal("3000.00"), TransactionType.INCOME,
                LocalDate.of(2026, 9, 1), alimentacao, user);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> result = transactionRepository.findWithFilters(
                user.getId(), null, null, TransactionType.EXPENSE, alimentacao.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Almoço");
    }

    @Test
    void deveSomarDespesasCorretamente() {
        User user = criarUsuario("arthur@teste.com");
        Category categoria = criarCategoria("Alimentação", user);

        criarTransacao("Almoço", new BigDecimal("30.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 10), categoria, user);
        criarTransacao("Jantar", new BigDecimal("45.50"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 11), categoria, user);

        BigDecimal total = transactionRepository.sumByUserIdAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, null, null);

        assertThat(total).isEqualByComparingTo("75.50");
    }

    @Test
    void deveRetornarZeroQuandoNaoHaTransacoes() {
        User user = criarUsuario("arthur@teste.com");

        BigDecimal total = transactionRepository.sumByUserIdAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, null, null);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deveAgruparDespesasPorCategoria() {
        User user = criarUsuario("arthur@teste.com");
        Category alimentacao = criarCategoria("Alimentação", user);
        Category transporte = criarCategoria("Transporte", user);

        criarTransacao("Almoço", new BigDecimal("30.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 10), alimentacao, user);
        criarTransacao("Jantar", new BigDecimal("20.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 11), alimentacao, user);
        criarTransacao("Uber", new BigDecimal("15.00"), TransactionType.EXPENSE,
                LocalDate.of(2026, 9, 12), transporte, user);

        List<com.arthurisidoro.personal_finance_api.dto.response.CategoryReportResponse> report =
                transactionRepository.findExpensesGroupedByCategory(user.getId(), null, null);

        assertThat(report).hasSize(2);
        assertThat(report.get(0).getCategoryName()).isEqualTo("Alimentação");
        assertThat(report.get(0).getTotalAmount()).isEqualByComparingTo("50.00");
    }

    private User criarUsuario(String email) {
        User user = new User();
        user.setName("Teste");
        user.setEmail(email);
        user.setPassword("senha-hash");
        entityManager.persist(user);
        return user;
    }

    private Category criarCategoria(String nome, User user) {
        Category category = new Category();
        category.setName(nome);
        category.setUser(user);
        entityManager.persist(category);
        return category;
    }

    private void criarTransacao(String descricao, BigDecimal valor, TransactionType tipo,
                                 LocalDate data, Category categoria, User user) {
        Transaction transaction = new Transaction();
        transaction.setDescription(descricao);
        transaction.setAmount(valor);
        transaction.setType(tipo);
        transaction.setDate(data);
        transaction.setCategory(categoria);
        transaction.setUser(user);
        entityManager.persist(transaction);
    }
}