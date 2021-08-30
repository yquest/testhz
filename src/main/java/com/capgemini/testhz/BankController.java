package com.capgemini.testhz;

import com.capgemini.cdao.AccountCDAO;
import com.capgemini.cdao.ClientCDAO;
import com.capgemini.client.Client;
import com.capgemini.mdao.AccountMDAO;
import com.capgemini.rest.AddAmountRequest;
import com.capgemini.rest.AddAmountResponse;
import com.capgemini.rest.NewAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BankController {
    @Autowired
    ClientCDAO clientCDAO;
    @Autowired
    AccountMDAO accountMDAO;
    @Autowired
    AccountCDAO accountCDAO;

    @PostMapping("create-client")
    String createClient(@RequestBody Client client) {
        clientCDAO.create(client);
        return "ok";
    }

    @GetMapping("all-clients")
    List<Client> all() {
        return clientCDAO.getAll().collect(Collectors.toList());
    }

    @DeleteMapping("remove-permission")
    String deletePermission(@RequestParam Integer client, @RequestParam Integer account) {
        accountMDAO.removeAccountPermission(client, account);
        return "ok";
    }

    @GetMapping("add-permission")
    String addPermission(@RequestParam Integer client, @RequestParam Integer account) {
        accountMDAO.addAccountPermission(client, account);
        return "ok";
    }

    @PostMapping("add-new-account")
    String addNewAccount(@RequestBody NewAccountRequest request) {
        accountCDAO.create(request.getAccount(), request.getClientIds(), request.getAmount());
        for (Integer clientId : request.getClientIds()) {
            accountMDAO.addAccountPermission(clientId, request.getAccount());
            clientCDAO.addAccountPermission(clientId, request.getAccount());
        }
        return "ok";
    }

    @PostMapping("add-amount")
    AddAmountResponse addAmount(@RequestBody AddAmountRequest request) {
        Map.Entry<Long, String> result = accountMDAO.addAmount(
                request.getIdAccount(),
                request.getIdClient(),
                request.getAmount()
        );
        return new AddAmountResponse(result.getKey(), result.getValue());
    }

    @PostMapping("dummy-post")
    AddAmountResponse dummyPost(@RequestBody AddAmountRequest request) {
        return new AddAmountResponse(0L, null);
    }

}
