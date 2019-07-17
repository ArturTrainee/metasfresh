/*
 * #%L
 * metasfresh-e2e
 * %%
 * Copyright (C) 2019 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import { DocumentStatusKey, DocumentActionKey } from '../../support/utils/constants';
import { BillingCandidates } from '../../page_objects/billing_candidates';
import { salesOrders } from '../../page_objects/sales_orders';
import { ShipmentDispositions } from '../../page_objects/shipment_dispositions';
import { SalesOrder, SalesOrderLine } from '../../support/utils/sales_order';
import { getLanguageSpecific } from '../../support/utils/utils';
import { salesInvoices } from '../../page_objects/sales_invoices';

describe('Void Sales Invoice and invoice the billing candidates again', function() {
  const businessPartnerName = 'Test Lieferant 1';
  const productName = 'Convenience Salat 250g';
  const originalQuantity = 200;
  const shipmentNotificationInboxText = `Lieferung [number] für Partner ${businessPartnerName} wurde erstellt.`;
  const shipmentNotificationModalText = 'Created: 1 WorkPackage Queue;';
  const generateInvoicesNotificationModalText =
    'Fakturlauf mit 1 Rechnungen eingeplant. Es sind bereits 0 zu erstellende Rechnungen in der Warteschlange, die vorher verarbeitet werden.';

  const salesInvoiceDocumentType = 'Sales Invoice';

  // for test
  let salesOrderDocumentNumber;
  let salesOrderRecordId;
  let totalAmountToPay = 0;

  before(function() {
    // This wait is stupid.
    // It also appears to be a good workaround for the problems in
    // cypress/support/utils/utils.js:1
    cy.wait(5000);
    cy.readAllNotifications();
  });

  it('Create Sales Order', function() {
    cy.fixture('misc/misc_dictionary.json').then(miscDictionary => {
      new SalesOrder()
        .setBPartner(businessPartnerName)
        .addLine(new SalesOrderLine().setProduct(productName).setQuantity(originalQuantity))
        .setDocumentAction(getLanguageSpecific(miscDictionary, DocumentActionKey.Complete))
        .setDocumentStatus(getLanguageSpecific(miscDictionary, DocumentStatusKey.Completed))
        .apply();
    });
  });

  it('Sales Order is Completed', function() {
    cy.expectDocumentStatus(DocumentStatusKey.Completed);
  });

  it('Save values needed for the next steps', function() {
    cy.getStringFieldValue('DocumentNo').then(documentNumber => {
      salesOrderDocumentNumber = documentNumber;
    });

    cy.getCurrentWindowRecordId().then(recordId => {
      salesOrderRecordId = recordId;
    });

    cy.openAdvancedEdit();
    cy.getStringFieldValue('Sum_Tax').then(function(val) {
      totalAmountToPay += parseFloat(val);
    });
    cy.getStringFieldValue('netsum').then(function(val) {
      totalAmountToPay += parseFloat(val);
    });
    cy.pressDoneButton();
  });

  it('Open the Referenced Billing Candidates', function() {
    cy.wait(1000);
    cy.openReferencedDocuments('C_Order_C_Invoice_Candidate');
  });

  it('Expect only 1 Billing Candidates row and open it', function() {
    BillingCandidates.getRows().should('have.length', 1);

    // select the first Table Row and click it (open it)
    // eslint-disable-next-line prettier/prettier
    BillingCandidates
      .getRows()
      .eq(0)
      .dblclick();
  });

  it('Billing Candidates checks after Sales Order created', function() {
    const qtyDelivered = ''; // backend is flaky and keeps switching between '' and '0' and back to ''
    const qtyInvoiced = ''; // backend is flaky and keeps switching between '' and '0' and back to ''

    checkBillingCandidate(qtyDelivered, qtyInvoiced);
  });

  describe('Ship the Sales Order', function() {
    it('Open the Referenced Shipment Disposition', function() {
      salesOrders.visit(salesOrderRecordId);
      cy.openReferencedDocuments('M_ShipmentSchedule');
    });

    it('Expect only 1 Shipment Disposition row and open it', function() {
      ShipmentDispositions.getRows().should('have.length', 1);

      // select the first Table Row and click it (open it)
      // eslint-disable-next-line prettier/prettier
      ShipmentDispositions
        .getRows()
        .eq(0)
        .dblclick();
    });

    it('Shipment Disposition checks', function() {
      cy.getStringFieldValue('C_BPartner_ID').should('contain', businessPartnerName);
      cy.getStringFieldValue('M_Product_ID').should('contain', productName);
      cy.getStringFieldValue('C_Order_ID').should('equal', salesOrderDocumentNumber);
      cy.getStringFieldValue('QtyOrdered_Calculated').should('equal', originalQuantity.toString(10));
      cy.getStringFieldValue('QtyToDeliver ').should('equal', originalQuantity.toString(10));
    });

    it('Execute action "Generate shipments', function() {
      cy.executeHeaderActionWithDialog('M_ShipmentSchedule_EnqueueSelection');
      cy.selectInListField('QuantityType', 'Quantity to deliver', true, null, true);
      cy.setCheckBoxValue('IsCompleteShipments', true, true);
      cy.setCheckBoxValue('IsShipToday', false, true);
      cy.pressStartButton();
      cy.getNotificationModal(shipmentNotificationModalText);
      cy.wait(5000);
      cy.getDOMNotificationsNumber().should('equal', 1);
      // todo check notification inbox text!
      cy.readAllNotifications();
    });

    it('Open the Referenced Billing Candidates', function() {
      salesOrders.visit(salesOrderRecordId);
      cy.openReferencedDocuments('C_Order_C_Invoice_Candidate');
    });

    it('Expect only 1 Billing Candidates row and open it', function() {
      BillingCandidates.getRows().should('have.length', 1);

      // select the first Table Row and click it (open it)
      // eslint-disable-next-line prettier/prettier
      BillingCandidates
        .getRows()
        .eq(0)
        .dblclick();
    });

    it('Billing Candidates checks after Shipment completion', function() {
      const qtyDelivered = originalQuantity.toString(10);
      const qtyInvoiced = '0';

      checkBillingCandidate(qtyDelivered, qtyInvoiced);
    });
  });

  describe('Generate Invoice and check it', function() {
    it('Execute action "Generate Invoices"', function() {
      cy.executeHeaderActionWithDialog('C_Invoice_Candidate_EnqueueSelectionForInvoicing');
      cy.pressStartButton(500);
      cy.wait(5000);

      cy.getNotificationModal(generateInvoicesNotificationModalText);
      cy.wait(5000);
      cy.getDOMNotificationsNumber().should('equal', 1);
      // todo check notification inbox text!
      // expected text:
      // Rechnung 145808 für Partner G0002 Test Lieferant 1 wurde erstellt.
      cy.readAllNotifications();
    });

    it('Billing Candidates checks after Invoice generation', function() {
      const qtyDelivered = originalQuantity.toString(10);
      const qtyInvoiced = originalQuantity.toString(10);

      checkBillingCandidate(qtyDelivered, qtyInvoiced);
    });
  });

  describe('Check first Sales Invoice', function() {
    it('Open referenced Sales Invoice', function() {
      cy.openReferencedDocuments('C_Invoice_Candidate_Sales_C_Invoice');
    });

    it('Expect only 1 Sales Invoice row and open it', function() {
      salesInvoices.getRows().should('have.length', 1);

      // eslint-disable-next-line prettier/prettier
      salesInvoices
        .getRows()
        .eq(0)
        .dblclick();
    });

    it('Sales Invoice checks', function() {
      salesInvoiceChecks(
        DocumentStatusKey.Completed,
        salesInvoiceDocumentType,
        businessPartnerName,
        false,
        productName,
        originalQuantity,
        totalAmountToPay
      );
    });
  });

  describe('Void the first Sales Invoice and check', function() {
    it('Void current Sales Invoice', function() {
      cy.fixture('misc/misc_dictionary.json').then(miscDictionary => {
        cy.processDocument(
          getLanguageSpecific(miscDictionary, DocumentActionKey.Void),
          getLanguageSpecific(miscDictionary, DocumentStatusKey.Reversed)
        );
      });
    });

    it('Expect Sales Invoice is Paid after Void', function() {
      const isPaid = true;
      cy.getCheckboxValue('IsPaid').should('equals', isPaid);
    });

    it('Check First Sales Invoice after Void', function() {
      const whichSI = 0; // 0 indexed
      const expectedNoRows = 2;

      visitSpecificReferencedSalesInvoice(whichSI, expectedNoRows);
      salesInvoiceChecks(
        DocumentStatusKey.Reversed,
        salesInvoiceDocumentType,
        businessPartnerName,
        true,
        productName,
        originalQuantity,
        totalAmountToPay
      );
    });
    it('Check Second Sales Invoice after Void', function() {
      const whichSI = 1; // 0 indexed
      const expectedNoRows = 2;

      visitSpecificReferencedSalesInvoice(whichSI, expectedNoRows);
      salesInvoiceChecks(
        DocumentStatusKey.Reversed,
        salesInvoiceDocumentType,
        businessPartnerName,
        true,
        productName,
        -1 * originalQuantity,
        -1 * totalAmountToPay
      );
    });
  });

  describe('Create second Sales Invoice and check', function() {
    it('Open the Referenced Billing Candidates', function() {
      salesOrders.visit(salesOrderRecordId);
      cy.openReferencedDocuments('C_Order_C_Invoice_Candidate');
    });

    it('Expect only 1 Billing Candidates row and open it', function() {
      BillingCandidates.getRows().should('have.length', 1);

      // select the first Table Row and click it (open it)
      // eslint-disable-next-line prettier/prettier
      BillingCandidates
        .getRows()
        .eq(0)
        .dblclick();
    });

    it('Execute action "Generate Invoices" the second time', function() {
      cy.executeHeaderActionWithDialog('C_Invoice_Candidate_EnqueueSelectionForInvoicing');
      cy.pressStartButton();

      cy.getNotificationModal(generateInvoicesNotificationModalText);
      cy.wait(5000);
      cy.getDOMNotificationsNumber().should('equal', 1);
      // todo check notification inbox text!
      // expected text:
      // Rechnung 145808 für Partner G0002 Test Lieferant 1 wurde erstellt.
      cy.readAllNotifications();
    });

    it('Expect 3 Sales Invoice and open the third', function() {
      const whichSI = 2; // 0 indexed
      const expectedNoRows = 3;

      visitSpecificReferencedSalesInvoice(whichSI, expectedNoRows);
    });

    it('Check the new Sales Invoice', function() {
      salesInvoiceChecks(
        DocumentStatusKey.Completed,
        salesInvoiceDocumentType,
        businessPartnerName,
        false,
        productName,
        originalQuantity,
        totalAmountToPay
      );
    });
  });

  ////////////////////////////////
  ////////////////////////////////
  ////////////////////////////////
  function visitSpecificReferencedSalesInvoice(whichSI, expectedNoRows) {
    salesOrders.visit(salesOrderRecordId);
    cy.openReferencedDocuments('AD_RelationType_ID-540160');
    salesInvoices.getRows().should('have.length', expectedNoRows);

    // eslint-disable-next-line prettier/prettier
    salesInvoices
      .getRows()
      .eq(whichSI)
      .dblclick();
  }

  function checkBillingCandidate(qtyDelivered, qtyInvoiced) {
    cy.getCheckboxValue('IsToRecompute').should('equals', false);
    cy.getStringFieldValue('Bill_BPartner_ID').should('contain', businessPartnerName);
    cy.getStringFieldValue('C_Order_ID').should('equal', salesOrderDocumentNumber);
    cy.getStringFieldValue('M_Product_ID').should('contain', productName);
    cy.getStringFieldValue('QtyEntered').should('equal', originalQuantity.toString(10));
    cy.getStringFieldValue('QtyOrdered').should('equal', originalQuantity.toString(10));
    cy.getStringFieldValue('QtyDelivered ').should('equal', qtyDelivered);
    cy.getStringFieldValue('QtyInvoiced ').should('equal', qtyInvoiced);
  }
});

// function clickOnNotificationWithText(text) {
//   cy.get('.header-item-badge.icon-lg i', { timeout: 10000 }).click();
//   cy.get('.inbox-item-unread .inbox-item-title')
//     .filter(':contains("' + text + '")')
//     .first()
//     .click();
// }

// eslint-disable-next-line prettier/prettier
function salesInvoiceChecks(documentStatusKey, salesInvoiceDocumentType, businessPartnerName, isPaid, productName, qtyEntered, totalAmountToPay) {
  cy.expectDocumentStatus(documentStatusKey);
  cy.getStringFieldValue('C_DocTypeTarget_ID').should('be.equal', salesInvoiceDocumentType);
  cy.getStringFieldValue('C_BPartner_ID').should('contains', businessPartnerName);
  cy.getCheckboxValue('IsPaid').should('equals', isPaid);

  cy.selectTab('C_InvoiceLine');
  cy.selectSingleTabRow();
  cy.openAdvancedEdit();
  cy.getStringFieldValue('M_Product_ID', true).should('contain', productName);
  cy.getStringFieldValue('QtyEntered', true).should('be.equal', qtyEntered.toString(10));
  cy.pressDoneButton();
  cy.getSalesInvoiceTotalAmount().then(function(amount) {
    expect(amount).equals(totalAmountToPay);
  });
}
